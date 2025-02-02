package de.jobst.resulter.application.config;

import de.jobst.resulter.domain.util.ResourceNotFoundException;
import de.jobst.resulter.domain.util.ResponseNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static void logError(Exception e) {
        log.error(e.getMessage());
        log.error(Arrays.toString(e.getStackTrace()));
        if (Objects.nonNull(e.getCause())) {
            log.error(e.getCause().getMessage());
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneralException(Exception ex, HttpServletRequest request) {
        logError(ex);
        return ResponseUtil.error(Collections.singletonList(ex.getMessage()),
            LocalizableString.of(MessageKeys.UNEXPECTED_ERROR),
            AdditionalStatusCodes.UNEXPECTED.value(),
            request.getRequestURI(),
            ex);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex,
                                                                               HttpServletRequest request) {
        logError(ex);
        return ResponseUtil.error(Collections.singletonList(ex.getMessage()),
            LocalizableString.of(MessageKeys.RESOURCE_NOT_FOUND),
            HttpStatus.NOT_FOUND.value(),
            request.getRequestURI(),
            ex);
    }

    @ExceptionHandler(ResponseNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResponseNotFoundException(ResponseNotFoundException ex,
                                                                               HttpServletRequest request) {
        logError(ex);
        return ResponseUtil.error(Collections.singletonList(ex.getMessage()),
            LocalizableString.of(MessageKeys.RESPONSE_NOT_FOUND),
            HttpStatus.NO_CONTENT.value(),
            request.getRequestURI(),
            ex);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolationException(DataIntegrityViolationException ex,
                                                                               HttpServletRequest request) {
        logError(ex);
        return ResponseUtil.error(Collections.singletonList(ex.getMessage()),
            LocalizableString.of(MessageKeys.DATA_INTEGRITY_VIOLATION),
            HttpStatus.CONFLICT.value(),
            request.getRequestURI(),
            ex);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex,
                                                                               HttpServletRequest request) {
        logError(ex);
        return ResponseUtil.error(Collections.singletonList(ex.getMessage()),
            LocalizableString.of(MessageKeys.ILLEGAL_ARGUMENT),
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI(),
            ex);
    }
}
