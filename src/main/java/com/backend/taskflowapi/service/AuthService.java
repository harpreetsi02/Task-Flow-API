package com.backend.taskflowapi.service;

import com.backend.taskflowapi.dto.request.LoginRequest;
import com.backend.taskflowapi.dto.request.RegisterRequest;
import com.backend.taskflowapi.dto.response.LoginResponse;
import com.backend.taskflowapi.dto.response.UserResponse;
import com.backend.taskflowapi.entity.AuthProvider;
import com.backend.taskflowapi.entity.Role;
import com.backend.taskflowapi.entity.User;
import com.backend.taskflowapi.exception.EmailAlreadyExistsException;
import com.backend.taskflowapi.exception.InvalidCredentialsException;
import com.backend.taskflowapi.mapper.UserMapper;
import com.backend.taskflowapi.repository.UserRepository;
import com.backend.taskflowapi.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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

    @Transactional
    public LoginResponse login(LoginRequest request){
        String email = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())){
            throw new InvalidCredentialsException();
        }

        String accessToken = jwtService.generateAccessToken(user);

        return new LoginResponse(accessToken, userMapper.toResponse(user));
    }
}
