package com.io.spring_boot_archetype.exception.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Generic exception response model.
 * This class is used to structure the error response sent to the client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenericExceptionResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
