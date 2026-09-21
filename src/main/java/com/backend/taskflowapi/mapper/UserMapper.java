package com.backend.taskflowapi.mapper;

import com.backend.taskflowapi.dto.response.UserResponse;
import com.backend.taskflowapi.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user){

        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
