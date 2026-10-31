package com.backend.taskflowapi.dto.response;

import com.backend.taskflowapi.enums.Role;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        Role role,
        LocalDateTime createdAt
) {}
