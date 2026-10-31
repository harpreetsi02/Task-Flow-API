package com.backend.taskflowapi.dto.request;

import com.backend.taskflowapi.enums.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record   UpdateProjectRequest(

        @NotBlank(message = "Project name is required!")
        @Size(max = 150, message = "Name must be at most 150 characters!")
        String name,

        @Size(max = 1000, message = "Description must be at most 1000 characters!")
        String description,

        @NotNull(message = "Status is required!")
        ProjectStatus status
) {
}
