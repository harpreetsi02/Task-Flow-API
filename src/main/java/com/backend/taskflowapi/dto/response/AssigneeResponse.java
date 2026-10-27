package com.backend.taskflowapi.dto.response;

public record AssigneeResponse (
        Long id,
        String fullName,
        String email
){
}
