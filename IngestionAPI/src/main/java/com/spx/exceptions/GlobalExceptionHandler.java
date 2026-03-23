package com.spx.exceptions;

import com.spx.dto.ApiErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Builder helper method of ApiErrorDTO
    private static ResponseEntity<ApiErrorDTO> buildResponseError(HttpStatus status, String errorTitle, String message, String action, String path) {

        ApiErrorDTO error = ApiErrorDTO.builder()
                .status(status.value())
                .errorTitle(errorTitle)
                .message(message)
                .action(action)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(error);
    }

    // JSON PARSING ERROR (400 BAD REQUEST)
    /*
     * This exception is thrown by Spring/Jackson when the HTTP request body
     * cannot be deserialized into the expected Java object.
     *
     * Typical causes:
     * - Malformed JSON syntax (missing brackets, commas, quotes)
     * - Incorrect data types (e.g., string instead of number)
     * - Completely invalid JSON structure
     *
     * This happens BEFORE the controller method is executed because Spring
     * tries to convert the request body into the @RequestBody DTO first.
     *
     * We return a 400 Bad Request because the client sent an invalid payload.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorDTO> handleMalformedJson(HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.error("Malformed JSON request on {} - {}", request.getRequestURI(), ex.getMessage());

        return buildResponseError(HttpStatus.BAD_REQUEST, "Bad Request", "Malformed JSON request", "Check request body format", request.getRequestURI());
    }

    // REQUEST BODY VALIDATION ERROR (400 BAD REQUEST)
    /*
     * This exception occurs when Bean Validation fails on the request body.
     *
     * It is triggered when a DTO annotated with @Valid contains fields that
     * violate validation constraints such as:
     *
     * - @NotNull
     * - @NotBlank
     * - @Size
     * - @Email
     * - etc.
     *
     * Spring validates the DTO automatically before the controller method
     * executes. If validation fails, a ConstraintViolationdException is thrown.
     *
     * We extract the first validation error to return a readable message
     * indicating which field failed validation.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorDTO> handleValidation(ConstraintViolationException ex, HttpServletRequest request) {

        log.error("Validation request on {} - {}", request.getRequestURI(), ex.getMessage());

        return buildResponseError(HttpStatus.BAD_REQUEST,"Validation Error","Request validation failed","Check request fields", request.getRequestURI());
    }


    // GENERIC UNEXPECTED ERROR (500 INTERNAL SERVER ERROR)
    /*
     * This is the global fallback handler for any exception not explicitly
     * handled by the other @ExceptionHandler methods.
     *
     * It prevents the application from returning raw stack traces or
     * framework-generated error responses to the client.
     *
     * Typical scenarios include:
     * - NullPointerException
     * - Unexpected runtime errors
     * - Bugs in the application logic
     *
     * The error is logged with full stack trace for debugging purposes,
     * while the client receives a generic 500 response.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDTO> handleGenericError(Exception ex, HttpServletRequest request) {

        log.error("Unexpected error on {} - {}", request.getRequestURI(), ex.getMessage());

        return buildResponseError(HttpStatus.INTERNAL_SERVER_ERROR,"Messaging Error","Failed to publish message to broker","Retry later",request.getRequestURI());
    }

    // RABBITMQ / MESSAGE BROKER ERROR (503 SERVICE UNAVAILABLE)
    /*
     * This exception occurs when the application fails to publish a message to the message broker (RabbitMQ).
     *
     * Possible causes include:
     * - RabbitMQ server is not reachable
     * - Connection issues
     * - Exchange or queue misconfiguration
     * - Broker temporarily unavailable
     *
     * Since the failure is related to an external infrastructure component,
     * we return a 503 Service Unavailable status indicating that the service
     * cannot process the request at this time but may succeed later.
     */
    @ExceptionHandler(AmqpException.class)
    public ResponseEntity<ApiErrorDTO> handleRabbitError(AmqpException ex, HttpServletRequest request) {

        log.error("RabbitMQ publish failed on {} - {}", request.getRequestURI(), ex.getMessage());

        return buildResponseError(HttpStatus.SERVICE_UNAVAILABLE,"Internal Server Error","Unexpected server error","Contact support", request.getRequestURI());
    }
}

