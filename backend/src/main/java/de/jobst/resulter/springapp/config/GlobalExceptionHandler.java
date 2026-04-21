package de.jobst.resulter.springapp.config;

import de.jobst.resulter.domain.util.OptimisticEntityLockException;
import de.jobst.resulter.domain.util.ResourceNotFoundException;
import de.jobst.resulter.domain.util.ResponseNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private ResponseEntity<ProblemDetail> toProblemDetail(
            HttpStatus status,
            MessageKey messageKey,
            List<String> errors,
            HttpServletRequest request,
            Exception ex,
            Integer legacyErrorCode) {

        log.error("Handling {} with status {} at path {}", ex.getClass().getSimpleName(), status.value(), request.getRequestURI(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setType(URI.create("https://resulter.dev/problems/" + messageKey.key()));
        problemDetail.setTitle(messageKey.key());
        problemDetail.setDetail(errors.isEmpty() ? messageKey.key() : errors.getFirst());
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("code", messageKey.key());
        problemDetail.setProperty("params", Map.of());
        problemDetail.setProperty("errors", errors);
        problemDetail.setProperty("timestamp", System.currentTimeMillis());
        problemDetail.setProperty("path", request.getRequestURI());
        if (legacyErrorCode != null) {
            problemDetail.setProperty("errorCode", legacyErrorCode);
        }

        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneralException(Exception ex, HttpServletRequest request) {
        return toProblemDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            MessageKeys.UNEXPECTED_ERROR,
            Collections.singletonList("An unexpected error occurred"),
            request,
            ex,
            AdditionalStatusCodes.UNEXPECTED.value());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFoundException(ResourceNotFoundException ex,
                                                                               HttpServletRequest request) {
        return toProblemDetail(
            HttpStatus.NOT_FOUND,
            MessageKeys.RESOURCE_NOT_FOUND,
            Collections.singletonList(ex.getMessage()),
            request,
            ex,
            HttpStatus.NOT_FOUND.value());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoResourceFoundException(NoResourceFoundException ex,
                                                                               HttpServletRequest request) {
        return toProblemDetail(
            HttpStatus.NOT_FOUND,
            MessageKeys.NO_RESOURCE_FOUND,
            Collections.singletonList(ex.getMessage()),
            request,
            ex,
            HttpStatus.NOT_FOUND.value());
    }

    @ExceptionHandler(ResponseNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResponseNotFoundException(ResponseNotFoundException ex,
                                                                               HttpServletRequest request) {
        return toProblemDetail(
            HttpStatus.NO_CONTENT,
            MessageKeys.RESPONSE_NOT_FOUND,
            Collections.singletonList(ex.getMessage()),
            request,
            ex,
            HttpStatus.NO_CONTENT.value());
    }

    @ExceptionHandler(OptimisticEntityLockException.class)
    public ResponseEntity<ProblemDetail> handleOptimisticEntityLockException(OptimisticEntityLockException ex,
                                                                                   HttpServletRequest request) {
        return toProblemDetail(
            HttpStatus.CONFLICT,
            MessageKeys.ENTITY_LOCK_CONFLICT,
            Collections.singletonList(ex.getMessage()),
            request,
            ex,
            HttpStatus.CONFLICT.value());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgumentException(IllegalArgumentException ex,
                                                                              HttpServletRequest request) {
        return toProblemDetail(
            HttpStatus.BAD_REQUEST,
            MessageKeys.BAD_REQUEST,
            Collections.singletonList(ex.getMessage()),
            request,
            ex,
            HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolationException(ConstraintViolationException ex,
                                                                                  HttpServletRequest request) {
        List<String> errors = ex.getConstraintViolations().stream()
            .map(violation -> MessageFormat.format("Invalid value ''{0}'' for {1}, {2}",
                violation.getInvalidValue(),
                violation.getPropertyPath(),
                violation.getMessage()))
            .toList();
        return toProblemDetail(
            HttpStatus.BAD_REQUEST,
            MessageKeys.BAD_REQUEST,
            errors,
            request,
            ex,
            HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException ex,
                                                                                HttpServletRequest request) {
        return toProblemDetail(
            HttpStatus.METHOD_NOT_ALLOWED,
            MessageKeys.METHOD_NOT_ALLOWED,
            Collections.singletonList(ex.getMessage()),
            request,
            ex,
            HttpStatus.METHOD_NOT_ALLOWED.value());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationExceptions(MethodArgumentNotValidException ex,
                                                                          HttpServletRequest request) {
        List<String> errors = new ArrayList<>();
        ex.getBindingResult()
            .getFieldErrors()
            .forEach(error -> errors.add(MessageFormat.format("{0}: {1}",
                error.getField(),
                error.getDefaultMessage())));
        return toProblemDetail(
            HttpStatus.BAD_REQUEST,
            MessageKeys.BAD_REQUEST,
            errors,
            request,
            ex,
            HttpStatus.BAD_REQUEST.value());
    }

}
