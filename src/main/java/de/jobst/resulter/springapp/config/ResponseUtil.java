package de.jobst.resulter.springapp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
public class ResponseUtil {

    private static void logError(Exception e) {
        log.error(e.getMessage());
        log.error(Arrays.toString(e.getStackTrace()));
        if (Objects.nonNull(e.getCause())) {
            log.error(e.getCause().getMessage());
        }
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(T data, LocalizableString message, String path) {
        return success(data, message, path, null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(T data, LocalizableString message, String path,
                                                             @Nullable CacheControl cacheControl) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        response.setErrors(null);
        response.setErrorCode(0); // No error
        response.setTimestamp(System.currentTimeMillis());
        response.setPath(path);
        return cacheControl != null ? ResponseEntity.ok().cacheControl(cacheControl).body(response) : ResponseEntity.ok(response);
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(List<String> errors, LocalizableString message, int errorCode,
                                                           String path,
                                                           Exception ex) {
        logError(ex);
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setData(null);
        response.setErrors(errors);
        response.setErrorCode(errorCode);
        response.setTimestamp(System.currentTimeMillis());
        response.setPath(path);
        if (null != HttpStatus.resolve(errorCode)) {
            return ResponseEntity.status(HttpStatusCode.valueOf(errorCode)).body(response);
        }
        return ResponseEntity.internalServerError().body(response);
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(LocalizableString message, int errorCode, String path,
                                                           Exception ex) {
        return error(Collections.singletonList(ex.getMessage()), message, errorCode, path, ex);
    }
}

