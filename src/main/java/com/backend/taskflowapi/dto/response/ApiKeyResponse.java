package com.backend.taskflowapi.dto.response;

import java.time.LocalDateTime;

public record ApiKeyResponse(
        Long id,
        String name,
        String keyPrefix,
        boolean revoked,
        LocalDateTime lastUsedAt,
        LocalDateTime createdAt
) {
}
