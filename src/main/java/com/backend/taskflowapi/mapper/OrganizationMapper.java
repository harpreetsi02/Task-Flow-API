package com.backend.taskflowapi.mapper;

import com.backend.taskflowapi.dto.response.OrganizationResponse;
import com.backend.taskflowapi.enums.OrgRole;
import com.backend.taskflowapi.entity.Organization;
import org.springframework.stereotype.Component;

@Component
public class OrganizationMapper {

    public OrganizationResponse toResponse(Organization organization, OrgRole currentUserRole){
        return new OrganizationResponse(
                organization.getId(),
                organization.getName(),
                organization.getSlug(),
                currentUserRole,
                organization.getCreatedAt()
        );
    }
}
