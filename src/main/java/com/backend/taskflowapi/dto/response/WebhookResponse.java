package com.backend.taskflowapi.dto.response;

import com.backend.taskflowapi.entity.WebhookEvenType;

import java.time.LocalDateTime;

public record WebhookResponse (
        Long id,
        String targetUrl,
        WebhookEvenType evenType,
        boolean active,
        LocalDateTime createdAt
){
}
