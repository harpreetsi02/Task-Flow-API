package com.backend.taskflowapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(

        @NotBlank(message = "Project name is required!")
        @Size(max = 150, message = "Name must be at most 150 characters!")
        String name,

        @Size(max = 1000, message = "Description must be at most 1000 characters!")
        String description
) {
}
