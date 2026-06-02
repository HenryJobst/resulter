package de.jobst.resulter.springapp.config;

import de.jobst.resulter.domain.util.OptimisticEntityLockException;
import de.jobst.resulter.domain.util.ResourceNotFoundException;
import de.jobst.resulter.domain.util.ResponseNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    GlobalExceptionHandler handler;
    HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/test/path");
    }

    @Test
    void handleGeneralException_returns500() {
        ResponseEntity<ProblemDetail> response =
                handler.handleGeneralException(new RuntimeException("boom"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProperties()).containsKey("errorCode");
    }

    @Test
    void handleResourceNotFoundException_returns404() {
        ResponseEntity<ProblemDetail> response =
                handler.handleResourceNotFoundException(new ResourceNotFoundException("not found"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ProblemDetail body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getDetail()).isEqualTo("not found");
        assertThat(body.getProperties()).containsEntry("errorCode", 404);
    }

    @Test
    void handleNoResourceFoundException_returns404() {
        var ex = new NoResourceFoundException(org.springframework.http.HttpMethod.GET, "/missing", "Not found");
        ResponseEntity<ProblemDetail> response =
                handler.handleNoResourceFoundException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void handleResponseNotFoundException_returns204() {
        ResponseEntity<ProblemDetail> response =
                handler.handleResponseNotFoundException(new ResponseNotFoundException("no content"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void handleOptimisticEntityLockException_returns409() {
        ResponseEntity<ProblemDetail> response =
                handler.handleOptimisticEntityLockException(new OptimisticEntityLockException("conflict"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).isEqualTo("conflict");
    }

    @Test
    void handleIllegalArgumentException_returns400() {
        ResponseEntity<ProblemDetail> response =
                handler.handleIllegalArgumentException(new IllegalArgumentException("bad arg"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).isEqualTo("bad arg");
    }

    @Test
    void handleConstraintViolationException_returns400WithErrors() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getInvalidValue()).thenReturn("badValue");
        var path = mock(jakarta.validation.Path.class);
        when(path.toString()).thenReturn("field");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must not be null");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));
        ResponseEntity<ProblemDetail> response =
                handler.handleConstraintViolationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        @SuppressWarnings("unchecked")
        var errors = (java.util.List<String>) response.getBody().getProperties().get("errors");
        assertThat(errors).isNotEmpty();
        assertThat(errors.getFirst()).contains("badValue").contains("must not be null");
    }

    @Test
    void handleMethodNotSupportedException_returns405() {
        var ex = new HttpRequestMethodNotSupportedException("DELETE");
        ResponseEntity<ProblemDetail> response =
                handler.handleMethodNotSupportedException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Test
    void handleValidationExceptions_returns400WithFieldErrors() {
        var bindingResult = mock(org.springframework.validation.BindingResult.class);
        var fieldError = new org.springframework.validation.FieldError("obj", "name", "must not be blank");
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(ex.getMessage()).thenReturn("Validation failed");

        ResponseEntity<ProblemDetail> response =
                handler.handleValidationExceptions(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        @SuppressWarnings("unchecked")
        var errors = (java.util.List<String>) response.getBody().getProperties().get("errors");
        assertThat(errors).isNotEmpty();
        assertThat(errors.getFirst()).contains("name").contains("must not be blank");
    }

    @Test
    void problemDetail_containsExpectedProperties() {
        ResponseEntity<ProblemDetail> response =
                handler.handleGeneralException(new RuntimeException("test"), request);

        ProblemDetail body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getProperties()).containsKeys("code", "params", "errors", "timestamp", "path");
        assertThat(body.getInstance()).hasToString("/test/path");
    }
}
