package com.backend.taskflowapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOrganizationRequest(

        @NotBlank(message = "Organization name is required!")
        @Size(max = 100, message = "Name must be at most 100 characters1")
        String name
) {
}
