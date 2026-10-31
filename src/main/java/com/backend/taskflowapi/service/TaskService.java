package com.backend.taskflowapi.service;

import com.backend.taskflowapi.dto.request.CreateTaskRequest;
import com.backend.taskflowapi.dto.request.UpdateTaskRequest;
import com.backend.taskflowapi.dto.response.TaskResponse;
import com.backend.taskflowapi.entity.*;
import com.backend.taskflowapi.enums.TaskStatus;
import com.backend.taskflowapi.exception.*;
import com.backend.taskflowapi.mapper.TaskMapper;
import com.backend.taskflowapi.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationAuthorizationService authorizationService;
    private final WebhookDispatchService webhookDispatchService;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    public TaskService(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            OrganizationRepository organizationRepository,
            OrganizationAuthorizationService authorizationService,
            WebhookDispatchService webhookDispatchService,
            MembershipRepository membershipRepository,
            UserRepository userRepository,
            TaskMapper taskMapper
    ) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.organizationRepository = organizationRepository;
        this.authorizationService = authorizationService;
        this.webhookDispatchService = webhookDispatchService;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.taskMapper = taskMapper;
    }

    @Transactional
    public TaskResponse createTask(
            Long organizationId, Long projectId,
            CreateTaskRequest request, User currentUser
    ){
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found with id: " + organizationId));

        authorizationService.requireMembership(currentUser, organization);

        Project project = projectRepository.findByIdAndOrganization(projectId, organization)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        Task task = new Task(
                request.title(),
                request.description(),
                project,
                request.priority(),
                currentUser
        );

        if (request.assigneeId() != null){
            User assignee = resolveAndValidateAssignee(request.assigneeId(), organization);
            task.setAssignee(assignee);
        }

        task.setDueDate(request.dueDate());

        taskRepository.save(task);

        return taskMapper.toResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(
            Long organizationId, Long projectId,
            Long taskId, UpdateTaskRequest request, User currentUser
    ){
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization is not found with id: " + organizationId));

        authorizationService.requireMembership(currentUser, organization);

        Project project = projectRepository.findByIdAndOrganization(projectId, organization)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        Task task = taskRepository.findByIdAndProject(taskId, project)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        TaskStatus previousTask = task.getStatus();

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());

        if (request.assigneeId() != null){
            User assignee = resolveAndValidateAssignee(request.assigneeId(), organization);
            task.setAssignee(assignee);
        } else {
            task.setAssignee(null);
        }

        if (previousTask != TaskStatus.DONE && task.getStatus() == TaskStatus.DONE){
            webhookDispatchService.dispatchTaskCompleted(task);
        }

        return taskMapper.toResponse(task);
    }

    public Page<TaskResponse> getTasks(
            Long organizationId, Long projectId,
            TaskStatus status, User currentUser, Pageable pageable
    ){
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found with id: " + organizationId));

        authorizationService.requireMembership(currentUser, organization);

        Project project = projectRepository.findByIdAndOrganization(projectId, organization)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        return taskRepository.findByProjectAndOptionalStatus(project, status, pageable)
                .map(taskMapper::toResponse);
    }

    @Transactional
    public void deleteTask(
            Long organizationId, Long projectId,
            Long taskId, User currentUser
    ){
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found with id: " + organizationId));

        authorizationService.requireMembership(currentUser, organization);

        Project project = projectRepository.findByIdAndOrganization(projectId, organization)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        Task task = taskRepository.findByIdAndProject(taskId, project)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        taskRepository.delete(task);
    }

    private User resolveAndValidateAssignee(Long assigneeId, Organization organization){
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new UserNotFoundException("assigneeId: " + assigneeId));

        boolean isMember = membershipRepository.existsByUserAndOrganization(assignee, organization);

        if (!isMember){
            throw new InvalidAssigneeException();
        }

        return assignee;
    }
}
