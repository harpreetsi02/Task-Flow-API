package com.backend.taskflowapi.controller;

import com.backend.taskflowapi.dto.request.CreateApiKeyRequest;
import com.backend.taskflowapi.dto.response.ApiKeyCreatedResponse;
import com.backend.taskflowapi.dto.response.ApiKeyResponse;
import com.backend.taskflowapi.security.CustomUserDetails;
import com.backend.taskflowapi.service.ApiKeyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/api-keys")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @PostMapping
    public ResponseEntity<ApiKeyCreatedResponse>  createApiKey(
            @PathVariable Long organizationId,
            @Valid @RequestBody CreateApiKeyRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ApiKeyCreatedResponse response =
                apiKeyService.createApiKey(organizationId, request, userDetails.getUser());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> getApiKeys(
            @PathVariable Long organizationId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<ApiKeyResponse> responses =
                apiKeyService.getApiKeys(organizationId, userDetails.getUser());

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{apiKeyId}")
    public ResponseEntity<Void> revokeApiKey(
            @PathVariable Long organizationId,
            @PathVariable Long apiKeyId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        apiKeyService.revokeApiKey(organizationId, apiKeyId, userDetails.getUser());

        return ResponseEntity.noContent().build();
    }
}
