package com.backend.taskflowapi.controller;

import com.backend.taskflowapi.dto.request.CreateWebhookRequest;
import com.backend.taskflowapi.dto.request.UpdateWebhookStatusRequest;
import com.backend.taskflowapi.dto.response.WebhookResponse;
import com.backend.taskflowapi.security.CustomUserDetails;
import com.backend.taskflowapi.service.WebhookSubscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations/{organizationId}/webhooks")
public class WebhookController {

    private final WebhookSubscriptionService webhookSubscriptionService;

    public WebhookController(WebhookSubscriptionService webhookSubscriptionService){
        this.webhookSubscriptionService = webhookSubscriptionService;
    }

    @PostMapping
    public ResponseEntity<WebhookResponse> createWebhook(
            @PathVariable Long organizationId,
            @Valid @RequestBody CreateWebhookRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        WebhookResponse response =
                webhookSubscriptionService.createWebhook(organizationId, request, userDetails.getUser());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<WebhookResponse>> getWebhooks(
            @PathVariable Long organizationId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<WebhookResponse> responses =
                webhookSubscriptionService.getWebhooks(organizationId, userDetails.getUser());

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{webhookId}")
    public ResponseEntity<Void> deleteWebhook(
            @PathVariable Long organizationId,
            @PathVariable Long webhookId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        webhookSubscriptionService.deleteWebhook(organizationId, webhookId, userDetails.getUser());

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{webhookId}")
    public ResponseEntity<WebhookResponse> updateWebhookStatus(
            @PathVariable Long organizationId,
            @PathVariable Long webhookId,
            @Valid @RequestBody UpdateWebhookStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        WebhookResponse response =
                webhookSubscriptionService.updateWebhookStatus(organizationId, webhookId, request, userDetails.getUser());

        return ResponseEntity.ok(response);
    }
}
