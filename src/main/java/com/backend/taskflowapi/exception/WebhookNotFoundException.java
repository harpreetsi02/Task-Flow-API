package com.backend.taskflowapi.exception;

public class WebhookNotFoundException extends RuntimeException{

    public WebhookNotFoundException(Long id){
        super("Webhook subscription not found with id: " + id);
    }
}
