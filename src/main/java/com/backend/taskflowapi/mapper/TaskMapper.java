package com.backend.taskflowapi.mapper;

import com.backend.taskflowapi.dto.response.AssigneeResponse;
import com.backend.taskflowapi.dto.response.TaskResponse;
import com.backend.taskflowapi.entity.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskResponse toResponse(Task task){
        AssigneeResponse assigneeResponse = task.getAssignee() != null
                ? new AssigneeResponse(
                        task.getAssignee().getId(),
                        task.getAssignee().getFullName(),
                        task.getAssignee().getEmail())
                : null;

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getProject().getId(),
                assigneeResponse,
                task.getAssignee().getFullName(),
                task.getDueDate(),
                task.getCreatedAt()
        );
    }
}
