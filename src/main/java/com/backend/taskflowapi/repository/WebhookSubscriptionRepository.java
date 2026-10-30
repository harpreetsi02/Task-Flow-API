package com.backend.taskflowapi.repository;

import com.backend.taskflowapi.entity.Organization;
import com.backend.taskflowapi.entity.WebhookEvenType;
import com.backend.taskflowapi.entity.WebhookSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WebhookSubscriptionRepository extends JpaRepository<WebhookSubscription, Long> {

    List<WebhookSubscription> findByOrganizationAndEvenTypeAndActiveTrue(
            Organization organization, WebhookEvenType evenType);

    List<WebhookSubscription> findByOrganization(Organization organization);

    Optional<WebhookSubscription> findByIdAndOrganization(Long id, Organization organization);
}
