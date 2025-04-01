package com.io.spring_boot_archetype.ratelimiter;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Aspect that intercepts controller endpoints and applies IP-based rate limiting based on the effective configuration
 * obtained from both class-level and method-level {@code @RateLimit} annotations.
 * <p>
 * When both are present, method-level values override class-level settings for that endpoint.
 * If no annotation is present, but global rate limiting is enabled via properties, the global settings are applied.
 * The rate limiting is now applied per IP address.
 * </p>
 */
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RateLimitProperties rateLimitProperties;

    // Map for fast, key-based access to RateLimiter instances.
    // Key is composed of the method's short signature and the client's IP address.
    private final ConcurrentHashMap<String, RateLimiter> limiterMap = new ConcurrentHashMap<>();

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object handleRateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        EffectiveRateLimitConfig config = resolveEffectiveConfig(joinPoint);

        // If rate limiting is not enabled, proceed with method execution.
        if (!config.enabled) {
            return joinPoint.proceed();
        }

        // Retrieve the client's IP address from the current request.
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String clientIp = request.getRemoteAddr();

        // Use a key composed of the method's short signature and the client IP.
        String key = joinPoint.getSignature().toShortString() + ":" + clientIp;
        // Retrieve or create a RateLimiter for the endpoint+IP combination.
        RateLimiter limiter = limiterMap.computeIfAbsent(key, k -> new RateLimiter(config.capacity, config.duration));

        if (!limiter.allowRequest()) {
            throw new RateLimitExceededException("Rate limit exceeded for IP " + clientIp + " on endpoint " + joinPoint.getSignature().toShortString());
        }
        return joinPoint.proceed();
    }

    /**
     * Resolves the effective rate limiting configuration by combining method-level and class-level annotations.
     * Method-level values override class-level values when present.
     *
     * @param joinPoint the join point representing the intercepted method.
     * @return an EffectiveRateLimitConfig instance containing the resolved settings.
     */
    private EffectiveRateLimitConfig resolveEffectiveConfig(ProceedingJoinPoint joinPoint) {
        EffectiveRateLimitConfig config = new EffectiveRateLimitConfig();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit methodAnno = method.getAnnotation(RateLimit.class);
        RateLimit classAnno = joinPoint.getTarget().getClass().getAnnotation(RateLimit.class);

        // If a method-level annotation is present, use its values (falling back to class-level if needed).
        if (methodAnno != null) {
            config.enabled = true;
            config.capacity = resolveCapacity(methodAnno, classAnno);
            config.duration = resolveDuration(methodAnno, classAnno);
        }
        // Otherwise, if a class-level annotation is present, use its values.
        else if (classAnno != null) {
            config.enabled = true;
            config.capacity = (classAnno.limit() > 0) ? classAnno.limit() : rateLimitProperties.getCapacity();
            config.duration = (classAnno.duration() > 0)
                    ? convertToMillis(classAnno.duration(), classAnno.unit())
                    : rateLimitProperties.getTime() * rateLimitProperties.getUnit().toMillis(1);
        }
        // Otherwise, check if global rate limiting is enabled.
        else if (rateLimitProperties.isEnabled()) {
            config.enabled = true;
            config.capacity = rateLimitProperties.getCapacity();
            config.duration = rateLimitProperties.getTime() * rateLimitProperties.getUnit().toMillis(1);
        } else {
            config.enabled = false;
        }
        return config;
    }

    /**
     * Converts a duration value and its time unit (provided as a string) to milliseconds.
     *
     * @param duration the duration value.
     * @param unit     the time unit as a string (e.g., "SECOND", "MINUTE", "HOUR").
     * @return the duration in milliseconds.
     * @throws IllegalArgumentException if the time unit does not match any of the allowed values.
     */
    private long convertToMillis(long duration, String unit) {
        if (unit == null) {
            throw new IllegalArgumentException("Time unit cannot be null.");
        }
        if (unit.equalsIgnoreCase("SECOND")) {
            return duration * 1000;
        } else if (unit.equalsIgnoreCase("MINUTE")) {
            return duration * 60 * 1000;
        } else if (unit.equalsIgnoreCase("HOUR")) {
            return duration * 3600 * 1000;
        } else {
            throw new IllegalArgumentException("Invalid time unit: " + unit + ". Expected SECOND, MINUTE, or HOUR.");
        }
    }

    /**
     * Simple POJO to hold the effective rate limiting configuration.
     */
    private static class EffectiveRateLimitConfig {
        boolean enabled;
        int capacity;
        long duration;
    }

    /**
     * Resolves the capacity for rate limiting based on method-level and class-level annotations.
     * If both are present, method-level values override class-level settings.
     *
     * @param methodAnno the method-level RateLimit annotation.
     * @param classAnno  the class-level RateLimit annotation.
     * @return the resolved capacity.
     */
    private int resolveCapacity(RateLimit methodAnno, RateLimit classAnno) {
        if (methodAnno.limit() > 0) {
            return methodAnno.limit();
        } else if (classAnno != null && classAnno.limit() > 0) {
            return classAnno.limit();
        } else {
            return rateLimitProperties.getCapacity();
        }
    }

    /**
     * Resolves the duration for rate limiting based on method-level and class-level annotations.
     * If both are present, method-level values override class-level settings.
     *
     * @param methodAnno the method-level RateLimit annotation.
     * @param classAnno  the class-level RateLimit annotation.
     * @return the resolved duration in milliseconds.
     */
    private long resolveDuration(RateLimit methodAnno, RateLimit classAnno) {
        if (methodAnno.duration() > 0) {
            return convertToMillis(methodAnno.duration(), methodAnno.unit());
        } else if (classAnno != null && classAnno.duration() > 0) {
            return convertToMillis(classAnno.duration(), classAnno.unit());
        } else {
            return rateLimitProperties.getTime() * rateLimitProperties.getUnit().toMillis(1);
        }
    }
}
