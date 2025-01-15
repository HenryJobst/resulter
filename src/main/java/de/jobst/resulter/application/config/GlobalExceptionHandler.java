package de.jobst.resulter.application.config;

import de.jobst.resulter.domain.util.ResourceNotFoundException;
import de.jobst.resulter.domain.util.ResponseNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;

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
}
