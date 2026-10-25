package com.backend.taskflowapi.repository;

import com.backend.taskflowapi.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    boolean existsBySlug(String slug);

    Optional<OrganizationRepository> findBySlug(String slug);
}
