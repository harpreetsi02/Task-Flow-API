package com.backend.taskflowapi.service;

import com.backend.taskflowapi.dto.request.CreateOrganizationRequest;
import com.backend.taskflowapi.dto.request.InviteMemberRequest;
import com.backend.taskflowapi.dto.response.OrganizationResponse;
import com.backend.taskflowapi.entity.Membership;
import com.backend.taskflowapi.entity.OrgRole;
import com.backend.taskflowapi.entity.Organization;
import com.backend.taskflowapi.entity.User;
import com.backend.taskflowapi.exception.*;
import com.backend.taskflowapi.mapper.OrganizationMapper;
import com.backend.taskflowapi.repository.MembershipRepository;
import com.backend.taskflowapi.repository.OrganizationRepository;
import com.backend.taskflowapi.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final MembershipRepository membershipRepository;
    private final OrganizationMapper organizationMapper;
    private final UserRepository userRepository;
    private final OrganizationAuthorizationService organizationAuthorizationService;

    public OrganizationService(
            OrganizationRepository organizationRepository,
            MembershipRepository membershipRepository,
            OrganizationMapper organizationMapper,
            UserRepository userRepository,
            OrganizationAuthorizationService organizationAuthorizationService
    ) {
        this.organizationRepository = organizationRepository;
        this.membershipRepository = membershipRepository;
        this.organizationMapper = organizationMapper;
        this.userRepository = userRepository;
        this.organizationAuthorizationService = organizationAuthorizationService;
    }

    @Transactional
    public OrganizationResponse createOrganization(CreateOrganizationRequest request, User currentUser){
        String slug = generateSlug(request.name());

        Organization organization = new Organization(request.name().trim(), slug);
        organizationRepository.save(organization);

        Membership ownerMembership = new Membership(currentUser, organization, OrgRole.OWNER);
        membershipRepository.save(ownerMembership);

        return organizationMapper.toResponse(organization, OrgRole.OWNER);
    }

    @Transactional
    public List<OrganizationResponse> getMyOrganizations(User currentUser){
        List<Membership> memberships = membershipRepository.findByUser(currentUser);

        return memberships.stream()
                .map(m -> organizationMapper.toResponse(m.getOrganization(), m.getRole()))
                .toList();
    }

    @Transactional
    public void inviteMember(Long organizationId, InviteMemberRequest request, User currentUser){
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException("This organization is not found: " + organizationId));

        organizationAuthorizationService.requireRole(
                currentUser, organization, Set.of(OrgRole.OWNER, OrgRole.ADMIN)
        );

        User invitedUser = userRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new UserNotFoundException(request.email()));

        if (membershipRepository.existsByUserAndOrganization(invitedUser, organization)){
            throw new AlreadyMemberException();
        }

        Membership newMembership = new Membership(invitedUser, organization, request.role());
        membershipRepository.save(newMembership);
    }

    private String generateSlug(String name){
        String baseSlug = name.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");

        String slug = baseSlug;
        int counter = 1;

        while (organizationRepository.existsBySlug(slug)){
            slug = baseSlug + "-" + counter;
            counter++;
        }

        return slug;
    }
}
