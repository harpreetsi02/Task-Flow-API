package com.backend.taskflowapi.dto.request;

import com.backend.taskflowapi.entity.TaskPriority;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateTaskRequest (

        @NotNull(message = "Title is required!")
        @Size(max = 200, message = "Title must be at most 200 characters!")
        String title,

        @Size(message = "Description must be at most 2000 characters!")
        String description,

        @NotNull(message = "Priority is required!")
        TaskPriority priority,

        Long assigneeId,

        LocalDate dueDate
){
}
