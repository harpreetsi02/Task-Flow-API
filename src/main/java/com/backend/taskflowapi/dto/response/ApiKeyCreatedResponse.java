package com.backend.taskflowapi.dto.response;

import java.time.LocalDateTime;

public record ApiKeyCreatedResponse(
        Long id,
        String name,
        String key,
        String keyPrefix,
        LocalDateTime createdAt
) {
}
