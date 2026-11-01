package com.backend.taskflowapi.controller;

import com.backend.taskflowapi.dto.request.CreateTaskRequest;
import com.backend.taskflowapi.dto.request.UpdateTaskRequest;
import com.backend.taskflowapi.dto.response.TaskResponse;
import com.backend.taskflowapi.dto.response.TaskResult;
import com.backend.taskflowapi.enums.TaskStatus;
import com.backend.taskflowapi.mapper.TaskMapper;
import com.backend.taskflowapi.security.CustomUserDetails;
import com.backend.taskflowapi.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/projects/{projectId}/tasks")
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    public TaskController(TaskService taskService, TaskMapper taskMapper){
        this.taskService = taskService;
        this.taskMapper = taskMapper;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable Long organizationId,
            @PathVariable Long projectId,
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TaskResult result =
                taskService.createTask(organizationId, projectId, request, userDetails.getUser());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(taskMapper.toResponseV1(result));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long organizationId,
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TaskResult result =
                taskService.updateTask(organizationId, projectId, taskId, request, userDetails.getUser());

        return ResponseEntity.ok(taskMapper.toResponseV1(result));
    }

    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getTasks(
            @PathVariable Long organizationId,
            @PathVariable Long projectId,
            @RequestParam(required = false) TaskStatus status,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable
    ) {
        Page<TaskResult> results =
                taskService.getTasks(organizationId, projectId, status, userDetails.getUser(), pageable);

        return ResponseEntity.ok(results.map(taskMapper::toResponseV1));
    }

    @DeleteMapping("{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long organizationId,
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        taskService.deleteTask(organizationId, projectId, taskId, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }
}
