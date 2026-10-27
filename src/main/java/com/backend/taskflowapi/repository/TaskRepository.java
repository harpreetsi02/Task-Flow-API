package com.backend.taskflowapi.repository;

import com.backend.taskflowapi.entity.Project;
import com.backend.taskflowapi.entity.Task;
import com.backend.taskflowapi.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByProject(Project project, Pageable pageable);

    Optional<Task> findByIdAndProject(Long id, Project project);

    @Query("SELECT t FROM Task t WHERE t.project = :project AND (:status IS NULL OR t.status = :status)")
    Page<Task> findByProjectAndOptionalStatus(
            @Param("project") Project project,
            @Param("status") TaskStatus status,
            Pageable pageable
    );
}
