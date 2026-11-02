package com.backend.taskflowapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateApiKeyRequest(
        @NotBlank(message = "Name is required!")
        @Size(max = 100, message = "Name must at most 100 characters!")
        String name
) {
}
