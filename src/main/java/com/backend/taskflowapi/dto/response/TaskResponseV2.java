package com.backend.taskflowapi.dto.response;

import com.backend.taskflowapi.enums.TaskPriority;
import com.backend.taskflowapi.enums.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponseV2(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        Long projectId,
        UserSummary assignee,
        UserSummary createdBy,
        LocalDate dueDate,
        LocalDateTime createdAt
) {
}
