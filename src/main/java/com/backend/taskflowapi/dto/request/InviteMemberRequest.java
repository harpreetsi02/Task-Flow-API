package com.backend.taskflowapi.dto.request;

import com.backend.taskflowapi.enums.OrgRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InviteMemberRequest(

        @NotBlank(message = "Email is required!")
        @Email(message = "Invalid email format!")
        String email,

        @NotNull(message = "Role is required!")
        OrgRole role
){
}
