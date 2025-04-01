package com.io.spring_boot_archetype.ratelimiter;

/**
 * Exception thrown when the rate limit is exceeded.
 */
public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message) {
        super(message);
    }
}
