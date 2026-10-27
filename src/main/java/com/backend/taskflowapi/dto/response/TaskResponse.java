package com.backend.taskflowapi.dto.response;

import com.backend.taskflowapi.entity.TaskPriority;
import com.backend.taskflowapi.entity.TaskStatus;

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
