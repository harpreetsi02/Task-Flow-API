package com.backend.taskflowapi.dto.response;

import com.backend.taskflowapi.enums.TaskPriority;
import com.backend.taskflowapi.enums.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        Long projectId,
        AssigneeResponse assignee,
        String createdByName,
        LocalDate dueDate,
        LocalDateTime createdAt
) {
}
