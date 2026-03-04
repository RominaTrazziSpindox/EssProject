package com.spx.exceptions;

import com.spx.dto.ApiErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Builder helper method of ApiErrorDTO
    private ApiErrorDTO buildError(HttpStatus status, String errorTitle, String message, String action, String path) {

        return ApiErrorDTO.builder()
                .status(status.value())
                .errorTitle(errorTitle)
                .message(message)
                .action(action)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }



    // JSON ERROR (400)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorDTO> handleMalformedJson(HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("Malformed JSON request on {} - {}", request.getRequestURI(), ex.getMessage());

        ApiErrorDTO error = buildError(HttpStatus.BAD_REQUEST,"Bad Request","Malformed JSON request",
                "Check request body format", request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(error);
    }


    // VALIDATION ERROR (400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDTO> handleValidationError(MethodArgumentNotValidException ex, HttpServletRequest request) {

        log.warn("Validation error on {} - {}", request.getRequestURI(), ex.getMessage());

        // Look for the validation message based upon fields
        String validationMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        ApiErrorDTO error = buildError( HttpStatus.BAD_REQUEST, "Validation Error", validationMessage,
                "Check request fields", request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(error);
    }


    // CONSTRAINT VIOLATION (400)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorDTO> handleConstraintViolation(ConstraintViolationException ex,HttpServletRequest request) {

        log.warn("Constraint violation on {} - {}", request.getRequestURI(), ex.getMessage());

        ApiErrorDTO error = buildError(HttpStatus.BAD_REQUEST, "Validation Error","Request validation failed",
                "Check request constraints", request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(error);
    }

    // INVALID ARGUMENT (400)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorDTO> handleIllegalArgument(IllegalArgumentException ex,HttpServletRequest request) {

        log.warn("Invalid request on {} - {}", request.getRequestURI(), ex.getMessage());

        ApiErrorDTO error = buildError(HttpStatus.BAD_REQUEST,"Bad Request", ex.getMessage(),
                "Verify request parameters", request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(error);
    }


    // SERVER ERROR (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDTO> handleGenericError(Exception ex,HttpServletRequest request) {

        log.error("Unexpected error on {}", request.getRequestURI(), ex);

        ApiErrorDTO error = buildError(HttpStatus.SERVICE_UNAVAILABLE,"Messaging Error","Failed to publish message to broker",
                "Retry later", request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // SERVER RABBITMQ PRODUCER UNAVAILABLE (503)
    @ExceptionHandler(AmqpException.class)
    public ResponseEntity<ApiErrorDTO> handleRabbitError(AmqpException ex,HttpServletRequest request) {

        log.error("RabbitMQ publish failed on {}", request.getRequestURI(), ex);

        ApiErrorDTO error = buildError(HttpStatus.INTERNAL_SERVER_ERROR,"Internal Server Error","Unexpected server error",
                "Contact support", request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }
}