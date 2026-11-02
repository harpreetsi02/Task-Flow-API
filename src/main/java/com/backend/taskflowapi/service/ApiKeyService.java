package com.backend.taskflowapi.service;

import com.backend.taskflowapi.dto.request.CreateApiKeyRequest;
import com.backend.taskflowapi.dto.response.ApiKeyCreatedResponse;
import com.backend.taskflowapi.dto.response.ApiKeyResponse;
import com.backend.taskflowapi.entity.ApiKey;
import com.backend.taskflowapi.entity.Organization;
import com.backend.taskflowapi.entity.User;
import com.backend.taskflowapi.enums.OrgRole;
import com.backend.taskflowapi.exception.ApiKeyNotFoundException;
import com.backend.taskflowapi.exception.OrganizationNotFoundException;
import com.backend.taskflowapi.repository.ApiKeyRepository;
import com.backend.taskflowapi.repository.OrganizationRepository;
import com.backend.taskflowapi.security.ApiKeyGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationAuthorizationService authorizationService;
    private final ApiKeyGenerator apiKeyGenerator;

    public ApiKeyService(
            ApiKeyRepository apiKeyRepository,
            OrganizationRepository organizationRepository,
            OrganizationAuthorizationService authorizationService,
            ApiKeyGenerator apiKeyGenerator
    ) {
        this.apiKeyRepository = apiKeyRepository;
        this.organizationRepository = organizationRepository;
        this.authorizationService = authorizationService;
        this.apiKeyGenerator = apiKeyGenerator;
    }

    @Transactional
    public ApiKeyCreatedResponse createApiKey(Long organizationId, CreateApiKeyRequest request, User currentUser){
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found with id: " + organizationId));

        authorizationService.requireRole(
                currentUser, organization, Set.of(OrgRole.ADMIN, OrgRole.OWNER)
        );

        String rawKey = apiKeyGenerator.generate();

        ApiKey apiKey = new ApiKey(
                organization,
                currentUser,
                request.name().trim(),
                rawKey.substring(0, 10),
                apiKeyGenerator.hash(rawKey)
        );
        apiKeyRepository.save(apiKey);

        return new ApiKeyCreatedResponse(
                apiKey.getId(), apiKey.getName(), rawKey,
                apiKey.getKeyPrefix(), apiKey.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<ApiKeyResponse> getApiKeys(Long organizationId, User currentUser){
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found with id: " + organizationId));

        authorizationService.requireRole(
                currentUser, organization, Set.of(OrgRole.ADMIN,  OrgRole.OWNER)
        );

        return apiKeyRepository.findByOrganization(organization)
                .stream()
                .map(apiKey -> new ApiKeyResponse(
                        apiKey.getId(),
                        apiKey.getName(),
                        apiKey.getKeyPrefix(),
                        apiKey.isRevoked(),
                        apiKey.getLastUsedAt(),
                        apiKey.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public void revokeApiKey(Long organizationId, Long apiKeyId, User currentUser){
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found with id: " + organizationId));

        authorizationService.requireRole(
                currentUser, organization, Set.of(OrgRole.ADMIN,  OrgRole.OWNER)
        );

        ApiKey apiKey = apiKeyRepository.findByIdAndOrganization(apiKeyId, organization)
                .orElseThrow(() -> new ApiKeyNotFoundException(apiKeyId));

        apiKey.setRevoked(true);
    }
}
