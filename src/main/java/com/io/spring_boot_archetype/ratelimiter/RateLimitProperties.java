package com.io.spring_boot_archetype.ratelimiter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Global configuration properties for rate limiting.
 * <p>
 * Default values:
 * - enabled: false (global rate limiting disabled)
 * - capacity: 10 (maximum number of requests allowed)
 * - time: 60 (time value)
 * - unit: SECONDS (time unit)
 * <p>
 * These values can be overridden in application.yml.
 */
@Data
@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {
    private boolean enabled = false;
    private int capacity = 10;
    private long time = 60;
    private TimeUnit unit = TimeUnit.SECONDS;
}
