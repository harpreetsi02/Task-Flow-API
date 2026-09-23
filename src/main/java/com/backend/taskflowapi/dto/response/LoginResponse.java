package com.backend.taskflowapi.dto.response;

public record LoginResponse(

        String accessToken,
        UserResponse user
) {
}
