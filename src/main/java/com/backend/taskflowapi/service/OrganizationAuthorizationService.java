package com.backend.taskflowapi.service;

import com.backend.taskflowapi.entity.Membership;
import com.backend.taskflowapi.enums.OrgRole;
import com.backend.taskflowapi.entity.Organization;
import com.backend.taskflowapi.entity.User;
import com.backend.taskflowapi.exception.InsufficientOrgPermissionException;
import com.backend.taskflowapi.exception.NotOrganizationMemberException;
import com.backend.taskflowapi.repository.MembershipRepository;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class OrganizationAuthorizationService {

    private final MembershipRepository membershipRepository;

    public OrganizationAuthorizationService(MembershipRepository membershipRepository){
        this.membershipRepository = membershipRepository;
    }

    public Membership requireMembership(User user, Organization organization){
        return membershipRepository.findByUserAndOrganization(user, organization)
                .orElseThrow(NotOrganizationMemberException::new);
    }

    public Membership requireRole(User user, Organization organization, Set<OrgRole> allowedRole){
        Membership membership = requireMembership(user, organization);

        if (!allowedRole.contains(membership.getRole())){
            throw new InsufficientOrgPermissionException();
        }

        return membership;
    }
}
