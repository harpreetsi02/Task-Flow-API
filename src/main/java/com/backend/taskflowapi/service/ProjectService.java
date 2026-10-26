package com.backend.taskflowapi.service;

import com.backend.taskflowapi.dto.request.CreateProjectRequest;
import com.backend.taskflowapi.dto.request.UpdateProjectRequest;
import com.backend.taskflowapi.dto.response.ProjectResponse;
import com.backend.taskflowapi.entity.OrgRole;
import com.backend.taskflowapi.entity.Organization;
import com.backend.taskflowapi.entity.Project;
import com.backend.taskflowapi.entity.User;
import com.backend.taskflowapi.exception.OrganizationNotFoundException;
import com.backend.taskflowapi.exception.ProjectNotFoundException;
import com.backend.taskflowapi.mapper.ProjectMapper;
import com.backend.taskflowapi.repository.OrganizationRepository;
import com.backend.taskflowapi.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final OrganizationAuthorizationService authorizationService;
    private final OrganizationRepository organizationRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            ProjectMapper projectMapper,
            OrganizationAuthorizationService authorizationService,
            OrganizationRepository organizationRepository
    ) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.authorizationService = authorizationService;
        this.organizationRepository = organizationRepository;
    }

    @Transactional
    public ProjectResponse createProject(Long organizationId, CreateProjectRequest request, User currentUser){
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found with id: " + organizationId));

        authorizationService.requireRole(
                currentUser, organization, Set.of(OrgRole.OWNER, OrgRole.ADMIN)
        );

        Project project = new Project(
                request.name(),
                request.description(),
                organization,
                currentUser
        );
        projectRepository.save(project);

        return projectMapper.toResponse(project);
    }

    public Page<ProjectResponse> getProjects(Long organizationId, User currentUser, Pageable pageable){
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found with id: " + organizationId));

        authorizationService.requireMembership(currentUser, organization);

        return projectRepository.findByOrganization(organization, pageable)
                .map(projectMapper::toResponse);
    }

    @Transactional
    public ProjectResponse updateProject(Long organizationId, Long projectId, UpdateProjectRequest request, User currentUser){

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found with id: " + organizationId));

        authorizationService.requireRole(
                currentUser, organization, Set.of(OrgRole.OWNER, OrgRole.ADMIN)
        );

        Project project = projectRepository.findByIdAndOrganization(projectId, organization)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        project.setName(request.name());
        project.setDescription(request.description());
        project.setStatus(request.status());

        return projectMapper.toResponse(project);
    }

    @Transactional
    public void deleteProject(Long organizationId, Long projectId, User currentUser){

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found with id: " + organizationId));

        authorizationService.requireRole(currentUser, organization, Set.of(OrgRole.OWNER));

        Project project = projectRepository.findByIdAndOrganization(projectId, organization)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        projectRepository.delete(project);
    }
}
