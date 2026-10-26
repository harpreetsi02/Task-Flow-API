package com.backend.taskflowapi.repository;

import com.backend.taskflowapi.entity.Organization;
import com.backend.taskflowapi.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Page<Project> findByOrganization(Organization organization, Pageable pageable);

    Optional<Project> findByIdAndOrganization(Long id, Organization organization);
}
