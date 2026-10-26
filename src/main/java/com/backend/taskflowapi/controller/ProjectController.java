package com.backend.taskflowapi.controller;

import com.backend.taskflowapi.dto.request.CreateProjectRequest;
import com.backend.taskflowapi.dto.request.UpdateProjectRequest;
import com.backend.taskflowapi.dto.response.ProjectResponse;
import com.backend.taskflowapi.security.CustomUserDetails;
import com.backend.taskflowapi.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizations/{organizationId}/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService){
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @PathVariable Long organizationId,
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ProjectResponse response =
                projectService.createProject(organizationId, request, userDetails.getUser());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ProjectResponse>> getProjects(
            @PathVariable Long organizationId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable
    ) {
        Page<ProjectResponse> responses =
                projectService.getProjects(organizationId, userDetails.getUser(), pageable);

        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long projectId,
            @PathVariable Long organizationId,
            @Valid @RequestBody UpdateProjectRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ProjectResponse response =
                projectService.updateProject(projectId, organizationId, request, userDetails.getUser());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long projectId,
            @PathVariable Long organizationId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        projectService.deleteProject(projectId, organizationId, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }
}
