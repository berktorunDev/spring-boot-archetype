package com.io.spring_boot_archetype.exception.model;

/**
 * Custom exception class for handling not found scenarios.
 * This exception can be thrown when a requested resource is not found.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
