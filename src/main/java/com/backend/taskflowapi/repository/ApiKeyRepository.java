package com.backend.taskflowapi.repository;

import com.backend.taskflowapi.entity.ApiKey;
import com.backend.taskflowapi.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    List<ApiKey> findByOrganization(Organization organization);

    Optional<ApiKey> findByIdAndOrganization(Long id, Organization organization);

    @Query("SELECT k FROM ApiKey k JOIN FETCH k.createdBy " +
            "WHERE k.keyHash = :hash AND k.revoked = false")
    Optional<ApiKey> findActiveByKeyHash(@Param("hash") String hash);
}
