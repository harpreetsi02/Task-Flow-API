package com.backend.taskflowapi.dto.response;

import com.backend.taskflowapi.entity.Role;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        Role role,
        LocalDateTime createdAt
){}
