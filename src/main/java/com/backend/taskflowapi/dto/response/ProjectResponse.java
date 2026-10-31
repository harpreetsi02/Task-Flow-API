package com.backend.taskflowapi.dto.response;

import com.backend.taskflowapi.enums.ProjectStatus;

import java.time.LocalDateTime;

public record ProjectResponse(

        Long id,
        String name,
        String description,
        ProjectStatus status,
        Long organizationId,
        String createdByName,
        LocalDateTime createdAt
) {
}
