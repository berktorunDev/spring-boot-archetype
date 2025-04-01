package com.io.spring_boot_archetype.ratelimiter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for applying rate limiting on a controller or method.
 * <p>
 * When placed on a class, it applies to all endpoints in that class.
 * If both class-level and method-level annotations are present,
 * the method-level values override the class-level settings for that endpoint.
 * The presence of this annotation indicates that rate limiting is enabled for that endpoint.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RateLimit {
    /**
     * Maximum number of requests allowed.
     * If a non-positive value is provided, the global default is used.
     */
    int limit() default -1;

    /**
     * Duration for rate limiting.
     * If a non-positive value is provided, the global default is used.
     */
    long duration() default -1;

    /**
     * Time unit for the duration, provided as a string.
     * Available options are "SECOND", "MINUTE", or "HOUR". Default is "SECOND".
     */
    String unit() default "SECOND";
}
