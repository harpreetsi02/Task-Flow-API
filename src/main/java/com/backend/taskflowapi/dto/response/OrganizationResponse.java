package com.backend.taskflowapi.dto.response;

import com.backend.taskflowapi.enums.OrgRole;

import java.time.LocalDateTime;

public record OrganizationResponse(
        Long id,
        String name,
        String slug,
        OrgRole currentUserRole,
        LocalDateTime createdAt
) {
}
