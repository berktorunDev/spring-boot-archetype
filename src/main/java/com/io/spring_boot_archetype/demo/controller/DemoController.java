package com.io.spring_boot_archetype.demo.controller;

import com.io.spring_boot_archetype.ratelimiter.RateLimit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * DemoController is a Spring Boot REST controller that demonstrates the use of rate limiting.
 * <p>
 * This controller contains endpoints that are rate limited to showcase the functionality.
 * </p>
 */
@RateLimit
@RestController
@RequestMapping("/api")
public class DemoController {

    /**
     * Endpoint to demonstrate rate limiting.
     * <p>
     * This endpoint is rate limited to 5 requests per minute.
     * </p>
     *
     * @return A greeting message.
     */
    @GetMapping("/hello")
    @RateLimit(limit = 5, duration = 60, unit = "second")
    public String hello() {
        return "Hello, World!";
    }

    /**
     * Endpoint to demonstrate a different greeting.
     *
     * @return A different greeting message.
     */
    @GetMapping("/greet")
    public String greet() {
        return "Greetings from the DemoController!";
    }
}
