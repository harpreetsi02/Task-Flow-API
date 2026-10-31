package com.backend.taskflowapi.dto.request;

import com.backend.taskflowapi.enums.TaskPriority;
import com.backend.taskflowapi.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateTaskRequest(

        @NotBlank(message = "title is required!")
        @Size(max = 200, message = "Title must be at most 200 characters!")
        String title,

        @Size(max = 2000, message = "Description must be at most 2000 characters!")
        String description,

        @NotNull(message = "Status is required!")
        TaskStatus status,

        @NotNull(message = "Priority is required!")
        TaskPriority priority,

        Long assigneeId,

        LocalDate dueDate
) {
}
