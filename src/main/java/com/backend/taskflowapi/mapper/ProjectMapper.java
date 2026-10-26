package com.backend.taskflowapi.mapper;

import com.backend.taskflowapi.dto.response.ProjectResponse;
import com.backend.taskflowapi.entity.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public ProjectResponse toResponse(Project project){

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getOrganization().getId(),
                project.getCreatedBy().getFullName(),
                project.getCreatedAt()
        );
    }

}
