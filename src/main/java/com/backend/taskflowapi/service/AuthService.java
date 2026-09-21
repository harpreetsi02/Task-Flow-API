package com.backend.taskflowapi.service;

import com.backend.taskflowapi.dto.request.RegisterRequest;
import com.backend.taskflowapi.dto.response.UserResponse;
import com.backend.taskflowapi.entity.AuthProvider;
import com.backend.taskflowapi.entity.Role;
import com.backend.taskflowapi.entity.User;
import com.backend.taskflowapi.exception.EmailAlreadyExistsException;
import com.backend.taskflowapi.mapper.UserMapper;
import com.backend.taskflowapi.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request){

        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        User user = new User(
                request.fullName().trim(),
                request.email(),
                passwordEncoder.encode(request.password()),
                Role.USER,
                AuthProvider.LOCAL
        );

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }
}
