package com.example.eventsystem.exception;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Slf4j
@Hidden
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, List<String>> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                ));

        return buildResponse(
                HttpStatus.BAD_REQUEST, "Validation failed", "Invalid input data", request, details, ex);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(
            HandlerMethodValidationException ex, HttpServletRequest request) {

        Map<String, List<String>> details = ex.getParameterValidationResults()
                .stream()
                .filter(result -> !result.getResolvableErrors().isEmpty())
                .collect(Collectors.toMap(
                        result -> result.getMethodParameter().getParameterName(),
                        result -> result.getResolvableErrors()
                                .stream()
                                .map(error -> error.getDefaultMessage() != null
                                        ? error.getDefaultMessage()
                                        : error.toString())
                                .collect(Collectors.toCollection(ArrayList::new)),
                        (left, right) -> {
                            left.addAll(right);
                            return left;
                        }
                ));

        return buildResponse(
                HttpStatus.BAD_REQUEST, "Validation failed", "Invalid input data", request, details, ex);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        Map<String, List<String>> details = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.groupingBy(
                        violation -> extractViolationPath(violation),
                        Collectors.mapping(ConstraintViolation::getMessage, Collectors.toList())
                ));

        return buildResponse(
                HttpStatus.BAD_REQUEST, "Validation failed", "Invalid input data", request, details, ex);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.NOT_FOUND, "Resource not found", ex.getMessage(), request, null, ex);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            ValidationException ex, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.BAD_REQUEST, "Business validation error", ex.getMessage(), request, null, ex);
    }

    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ErrorResponse> handleConflict(
            Exception ex, HttpServletRequest request) {
        String message = (ex instanceof DataIntegrityViolationException)
                ? "Database integrity constraint violation"
                : ex.getMessage();
        return buildResponse(HttpStatus.CONFLICT, "Conflict", message, request, null, ex);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Malformed JSON",
                "Check your request body format", request, null, ex);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        Map<String, List<String>> details = Map.of(
                ex.getParameterName(),
                List.of("Required request parameter is missing")
        );

        return buildResponse(HttpStatus.BAD_REQUEST, "Missing request parameter",
                ex.getMessage(), request, details, ex);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String expectedType = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName()
                : "required type";
        Map<String, List<String>> details = Map.of(
                ex.getName(),
                List.of("Expected value of type " + expectedType)
        );

        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid request parameter",
                ex.getMessage(), request, details, ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(
            Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error", "An unexpected error occurred", request, null, ex);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status, String error, String message,
            HttpServletRequest request, Map<String, ?> details, Exception ex) {

        if (status.is5xxServerError()) {
            log.error("SERVER ERROR at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        } else {
            log.warn("CLIENT ERROR at {}: {} - {}", request.getRequestURI(), status.value(), message);
        }

        ErrorResponse response = new ErrorResponse(
                status.value(),
                error,
                message,
                request.getRequestURI(),
                details
        );

        return ResponseEntity.status(status).body(response);
    }

    private String extractViolationPath(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        int lastDotIndex = path.lastIndexOf('.');
        return lastDotIndex >= 0 ? path.substring(lastDotIndex + 1) : path;
    }
}
