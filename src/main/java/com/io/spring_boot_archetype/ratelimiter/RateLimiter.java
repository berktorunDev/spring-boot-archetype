package com.io.spring_boot_archetype.ratelimiter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A thread-safe rate limiter that restricts the number of requests within a specified time period.
 * This implementation uses atomic variables and CAS loops to ensure atomic operations.
 */
public class RateLimiter {
    private final int limit;
    private final long duration;
    private final AtomicInteger requests = new AtomicInteger(0);
    private final AtomicLong windowStart = new AtomicLong(System.currentTimeMillis());

    public RateLimiter(int limit, long duration) {
        this.limit = limit;
        this.duration = duration;
    }

    /**
     * Checks if a new request is allowed based on the current rate limiting policy.
     *
     * @return true if the request is allowed, false otherwise.
     */
    public boolean allowRequest() {
        long now = System.currentTimeMillis();
        long currentWindow = windowStart.get();
        // If the duration has passed and we can atomically update the window start, reset the request count.
        if (now - currentWindow > duration && windowStart.compareAndSet(currentWindow, now)) {
            requests.set(0);
        }
        // Atomically check and increment the request counter.
        while (true) {
            int current = requests.get();
            if (current >= limit) {
                return false;
            }
            if (requests.compareAndSet(current, current + 1)) {
                return true;
            }
        }
    }
}
