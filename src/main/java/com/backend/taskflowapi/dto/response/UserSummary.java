package com.backend.taskflowapi.dto.response;

public record UserSummary(
        Long id,
        String fullName,
        String email
) {
}
