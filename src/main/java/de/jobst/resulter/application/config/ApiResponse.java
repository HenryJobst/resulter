package de.jobst.resulter.application.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ApiResponse<T> {
    private boolean success; // was successful or not
    private LocalizableString message; // message to describe the result
    private T data; // actual response data of type T (can be an entity, a list, or any other object)
    private List<String> errors; // list of errors that describe what went wrong if the request failed
    private int errorCode; // integer error code that can be used to classify different types of errors
    private long timestamp; // long representing the time the response was generated
    private String path; // URL path of the request, which helps in debugging
}
