package com.backend.taskflowapi.exception;

public class ApiKeyNotFoundException extends RuntimeException{

    public ApiKeyNotFoundException(Long id) {
        super("ApiKey not found with id: " + id);
    }
}
