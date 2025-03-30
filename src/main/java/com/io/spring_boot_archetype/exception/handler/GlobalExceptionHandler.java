package com.io.spring_boot_archetype.exception.handler;

import com.io.spring_boot_archetype.exception.model.GenericExceptionResponse;
import com.io.spring_boot_archetype.exception.model.NotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles NotFoundException.
     * Thrown when a requested resource is not found.
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<GenericExceptionResponse> handleNotFoundException(
            NotFoundException ex, WebRequest request) {
        log.error("Resource not found: {}", ex.getMessage(), ex);
        return createErrorResponse(HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getDescription(false));
    }

    /**
     * Handles NoHandlerFoundException.
     * Thrown when no handler is found for the requested URL.
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<GenericExceptionResponse> handleNoHandlerFoundException(
            NoHandlerFoundException ex, WebRequest request) {
        log.error("No handler found for request: {}", ex.getRequestURL(), ex);
        return createErrorResponse(HttpStatus.NOT_FOUND,
                "No handler found for " + ex.getRequestURL(),
                request.getDescription(false));
    }

    /**
     * Handles MethodArgumentNotValidException.
     * Thrown when validation on an argument annotated with @Valid fails.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericExceptionResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, WebRequest request) {
        String errorMsg = "Validation failed: " + ex.getBindingResult()
                .getAllErrors().getFirst().getDefaultMessage();
        log.error("Validation error: {}", errorMsg, ex);
        return createErrorResponse(HttpStatus.BAD_REQUEST, errorMsg, request.getDescription(false));
    }

    /**
     * Handles BindException.
     * Thrown when binding errors occur, such as type mismatches in request parameters.
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<GenericExceptionResponse> handleBindException(
            BindException ex, WebRequest request) {
        String errorMsg = "Binding error: " + ex.getAllErrors().getFirst().getDefaultMessage();
        log.error("Binding error: {}", errorMsg, ex);
        return createErrorResponse(HttpStatus.BAD_REQUEST, errorMsg, request.getDescription(false));
    }

    /**
     * Handles MissingServletRequestParameterException.
     * Thrown when a required request parameter is missing.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<GenericExceptionResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, WebRequest request) {
        String errorMsg = "Missing request parameter: " + ex.getParameterName();
        log.error("Missing request parameter: {}", ex.getParameterName(), ex);
        return createErrorResponse(HttpStatus.BAD_REQUEST, errorMsg, request.getDescription(false));
    }

    /**
     * Handles MethodArgumentTypeMismatchException.
     * Thrown when a method argument is not of the expected type.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<GenericExceptionResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        String errorMsg = "Type mismatch for parameter: " + ex.getName();
        log.error("Method argument type mismatch: {}", errorMsg, ex);
        return createErrorResponse(HttpStatus.BAD_REQUEST, errorMsg, request.getDescription(false));
    }

    /**
     * Handles HttpMessageNotReadableException.
     * Thrown when the request body is not readable, e.g., due to malformed JSON.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GenericExceptionResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, WebRequest request) {
        log.error("Malformed JSON request: {}", ex.getMessage(), ex);
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Malformed JSON request", request.getDescription(false));
    }

    /**
     * Handles ConstraintViolationException.
     * Thrown when a validation constraint is violated.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<GenericExceptionResponse> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        String errorMsg = "Constraint violation: " + ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.error("Constraint violation: {}", errorMsg, ex);
        return createErrorResponse(HttpStatus.BAD_REQUEST, errorMsg, request.getDescription(false));
    }

    /**
     * Handles HttpRequestMethodNotSupportedException.
     * Thrown when the HTTP request method is not supported by the endpoint.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<GenericExceptionResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {
        log.error("HTTP method not supported: {}", ex.getMessage(), ex);
        return createErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), request.getDescription(false));
    }

    /**
     * Handles HttpMediaTypeNotSupportedException.
     * Thrown when the request's media type is not supported.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<GenericExceptionResponse> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex, WebRequest request) {
        log.error("HTTP media type not supported: {}", ex.getMessage(), ex);
        return createErrorResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage(), request.getDescription(false));
    }

    /**
     * Handles HttpMediaTypeNotAcceptableException.
     * Thrown when the requested media type is not acceptable.
     */
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<GenericExceptionResponse> handleHttpMediaTypeNotAcceptableException(
            HttpMediaTypeNotAcceptableException ex, WebRequest request) {
        log.error("HTTP media type not acceptable: {}", ex.getMessage(), ex);
        return createErrorResponse(HttpStatus.NOT_ACCEPTABLE, ex.getMessage(), request.getDescription(false));
    }

    /**
     * Handles BadCredentialsException.
     * Thrown when authentication fails due to invalid credentials.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<GenericExceptionResponse> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        log.error("Bad credentials: {}", ex.getMessage(), ex);
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid username or password", request.getDescription(false));
    }

    /**
     * Handles NoResourceFoundException.
     * Thrown when a specific resource is not found in business logic.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<GenericExceptionResponse> handleNoResourceFoundException(
            NoResourceFoundException ex, WebRequest request) {
        log.error("No resource found: {}", ex.getMessage(), ex);
        return createErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getDescription(false));
    }

    /**
     * Handles AccessDeniedException.
     * Thrown when a user attempts to access a resource without proper authorization.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<GenericExceptionResponse> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        log.error("Access denied: {}", ex.getMessage(), ex);
        return createErrorResponse(HttpStatus.FORBIDDEN, "Access is denied", request.getDescription(false));
    }

    /**
     * Handles all other uncaught exceptions.
     * Acts as a fallback for any exceptions that are not explicitly handled.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericExceptionResponse> handleAllUncaughtException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request.getDescription(false));
    }

    /**
     * Utility method to build a structured error response.
     *
     * @param status  HTTP status to be returned
     * @param message Error message
     * @param path    Request path description
     * @return ResponseEntity containing the error response
     */
    private ResponseEntity<GenericExceptionResponse> createErrorResponse(
            HttpStatus status, String message, String path) {
        GenericExceptionResponse errorResponse = GenericExceptionResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }
}
