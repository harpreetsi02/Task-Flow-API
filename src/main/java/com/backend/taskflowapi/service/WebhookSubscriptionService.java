package com.backend.taskflowapi.service;

import com.backend.taskflowapi.dto.request.CreateWebhookRequest;
import com.backend.taskflowapi.dto.response.WebhookResponse;
import com.backend.taskflowapi.entity.OrgRole;
import com.backend.taskflowapi.entity.Organization;
import com.backend.taskflowapi.entity.User;
import com.backend.taskflowapi.entity.WebhookSubscription;
import com.backend.taskflowapi.exception.OrganizationNotFoundException;
import com.backend.taskflowapi.mapper.WebhookMapper;
import com.backend.taskflowapi.repository.OrganizationRepository;
import com.backend.taskflowapi.repository.WebhookSubscriptionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class WebhookSubscriptionService {

    private final WebhookSubscriptionRepository webhookSubscriptionRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationAuthorizationService authorizationService;
    private final WebhookMapper webhookMapper;

    public WebhookSubscriptionService(
            WebhookSubscriptionRepository webhookSubscriptionRepository,
            OrganizationRepository organizationRepository,
            OrganizationAuthorizationService authorizationService,
            WebhookMapper webhookMapper
    ) {
        this.webhookSubscriptionRepository = webhookSubscriptionRepository;
        this.organizationRepository = organizationRepository;
        this.authorizationService = authorizationService;
        this.webhookMapper = webhookMapper;
    }

    @Transactional
    public WebhookResponse createWebhook(Long organizationId, CreateWebhookRequest request, User currentUser){
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found with id: " + organizationId));

        authorizationService.requireRole(currentUser, organization, Set.of(OrgRole.ADMIN, OrgRole.OWNER));

        WebhookSubscription subscription = new WebhookSubscription(
                organization,
                request.targetUrl(),
                request.evenType()
        );

        webhookSubscriptionRepository.save(subscription);

        return webhookMapper.toResponse(subscription);
    }

    public List<WebhookResponse> getWebhooks(Long organizationId, User currentUser){
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found with id: " + organizationId));

        authorizationService.requireRole(currentUser, organization, Set.of(OrgRole.OWNER, OrgRole.ADMIN));

        return webhookSubscriptionRepository.findByOrganization(organization)
                .stream()
                .map(webhookMapper::toResponse)
                .toList();
    }
}
