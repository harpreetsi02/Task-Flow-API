package com.backend.taskflowapi.mapper;

import com.backend.taskflowapi.dto.response.*;
import com.backend.taskflowapi.entity.Task;
import com.backend.taskflowapi.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskResult toResult(Task task) {
        UserSummary assignee = task.getAssignee() != null
                ? toUserSummary(task.getAssignee())
                : null;

        return  new TaskResult(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getProject().getId(),
                assignee,
                toUserSummary(task.getCreatedBy()),
                task.getDueDate(),
                task.getCreatedAt()
        );
    }

    public TaskResponse toResponseV1(TaskResult result){
        AssigneeResponse assignee = result.assignee() != null
                ? new AssigneeResponse(
                        result.assignee().id(),
                        result.assignee().fullName(),
                        result.assignee().email())
                : null;

        return new TaskResponse(
                result.id(),
                result.title(),
                result.description(),
                result.status(),
                result.priority(),
                result.projectId(),
                assignee,
                result.createdBy().fullName(),
                result.dueDate(),
                result.createdAt()
        );
    }

    public TaskResponseV2 toResponseV2(TaskResult result){
        return new TaskResponseV2(
                result.id(),
                result.title(),
                result.description(),
                result.status(),
                result.priority(),
                result.projectId(),
                result.assignee(),
                result.createdBy(),
                result.dueDate(),
                result.createdAt()
        );
    }

    private UserSummary toUserSummary(User user){
        return new UserSummary(user.getId(), user.getFullName(), user.getEmail());
    }
}
