package de.jobst.resulter.application.config;

import de.jobst.resulter.domain.util.OptimisticEntityLockException;
import de.jobst.resulter.domain.util.ResourceNotFoundException;
import de.jobst.resulter.domain.util.ResponseNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneralException(Exception ex, HttpServletRequest request) {
        return ResponseUtil.error(Collections.singletonList(ex.getMessage()),
            LocalizableString.of(MessageKeys.UNEXPECTED_ERROR),
            AdditionalStatusCodes.UNEXPECTED.value(),
            request.getRequestURI(),
            ex);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex,
                                                                               HttpServletRequest request) {
        return ResponseUtil.error(Collections.singletonList(ex.getMessage()),
            LocalizableString.of(MessageKeys.RESOURCE_NOT_FOUND),
            HttpStatus.NOT_FOUND.value(),
            request.getRequestURI(),
            ex);
    }

    @ExceptionHandler(ResponseNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResponseNotFoundException(ResponseNotFoundException ex,
                                                                               HttpServletRequest request) {
        return ResponseUtil.error(Collections.singletonList(ex.getMessage()),
            LocalizableString.of(MessageKeys.RESPONSE_NOT_FOUND),
            HttpStatus.NO_CONTENT.value(),
            request.getRequestURI(),
            ex);
    }

    @ExceptionHandler(OptimisticEntityLockException.class)
    public ResponseEntity<ApiResponse<Object>> handleOptimisticEntityLockException(OptimisticEntityLockException ex,
                                                                                   HttpServletRequest request) {
        return ResponseUtil.error(Collections.singletonList(ex.getMessage()),
            LocalizableString.of(MessageKeys.ENTITY_LOCK_CONFLICT),
            HttpStatus.CONFLICT.value(),
            request.getRequestURI(),
            ex);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex,
                                                                              HttpServletRequest request) {
        return ResponseUtil.error(Collections.singletonList(ex.getMessage()),
            LocalizableString.of(MessageKeys.BAD_REQUEST),
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI(),
            ex);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolationException(ConstraintViolationException ex,
                                                                                  HttpServletRequest request) {
        List<String> errors = ex.getConstraintViolations().stream()
            .map(violation -> MessageFormat.format("Invalid value ''{0}'' for {1}, {2}",
                violation.getInvalidValue(),
                violation.getPropertyPath(),
                violation.getMessage()))
            .toList();
        return ResponseUtil.error(errors,
            LocalizableString.of(MessageKeys.BAD_REQUEST),
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI(),
            ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex,
                                                                          HttpServletRequest request) {
        List<String> errors = new ArrayList<>();
        ex.getBindingResult()
            .getFieldErrors()
            .forEach(error -> errors.add(MessageFormat.format("{0}: {1}",
                error.getField(),
                error.getDefaultMessage())));
        return ResponseUtil.error(errors,
            LocalizableString.of(MessageKeys.BAD_REQUEST),
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI(),
            ex);
    }

}
