package com.backend.taskflowapi.repository;

import com.backend.taskflowapi.entity.Membership;
import com.backend.taskflowapi.entity.Organization;
import com.backend.taskflowapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    Optional<Membership> findByUserAndOrganization(User user, Organization organization);

    List<Membership> findByUser(User user);

    List<Membership> findByOrganization(Organization organization);

    boolean existsByUserAndOrganization(User user, Organization organization);
}
