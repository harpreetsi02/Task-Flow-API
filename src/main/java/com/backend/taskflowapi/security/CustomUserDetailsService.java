package com.backend.taskflowapi.security;

import com.backend.taskflowapi.entity.User;
import com.backend.taskflowapi.exception.UserNotFoundException;
import com.backend.taskflowapi.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found: " + email
                        )
                );

        return new CustomUserDetails(user);
    }
}
