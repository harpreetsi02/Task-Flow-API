package com.backend.taskflowapi.controller;

import com.backend.taskflowapi.dto.request.CreateOrganizationRequest;
import com.backend.taskflowapi.dto.request.InviteMemberRequest;
import com.backend.taskflowapi.dto.response.OrganizationResponse;
import com.backend.taskflowapi.entity.User;
import com.backend.taskflowapi.security.CustomUserDetails;
import com.backend.taskflowapi.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService){
        this.organizationService = organizationService;
    }

    @PostMapping
    public ResponseEntity<OrganizationResponse> create(
            @Valid @RequestBody CreateOrganizationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User currentUser = userDetails.getUser();
        OrganizationResponse response = organizationService.createOrganization(request, currentUser);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrganizationResponse>> getMyOrganizations(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<OrganizationResponse> responses =
                organizationService.getMyOrganizations(userDetails.getUser());

        return ResponseEntity.ok(responses);
    }

    @PostMapping("{organizationId}/members")
    public ResponseEntity<Void> inviteMember(
            @PathVariable Long organizationId,
            @Valid @RequestBody InviteMemberRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        organizationService.inviteMember(organizationId, request, userDetails.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
