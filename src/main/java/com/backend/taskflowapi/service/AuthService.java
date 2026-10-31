package com.backend.taskflowapi.service;

import com.backend.taskflowapi.dto.request.LoginRequest;
import com.backend.taskflowapi.dto.request.RefreshTokenRequest;
import com.backend.taskflowapi.dto.request.RegisterRequest;
import com.backend.taskflowapi.dto.response.LoginResponse;
import com.backend.taskflowapi.dto.response.UserResponse;
import com.backend.taskflowapi.enums.AuthProvider;
import com.backend.taskflowapi.entity.RefreshToken;
import com.backend.taskflowapi.enums.Role;
import com.backend.taskflowapi.entity.User;
import com.backend.taskflowapi.exception.EmailAlreadyExistsException;
import com.backend.taskflowapi.exception.InvalidCredentialsException;
import com.backend.taskflowapi.exception.InvalidRefreshTokenException;
import com.backend.taskflowapi.mapper.UserMapper;
import com.backend.taskflowapi.repository.RefreshTokenRepository;
import com.backend.taskflowapi.repository.UserRepository;
import com.backend.taskflowapi.security.JwtService;
import com.backend.taskflowapi.security.RefreshTokenAuthHelper;
import com.backend.taskflowapi.security.TokenBlacklistService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistService tokenBlacklistService;
    private final RefreshTokenAuthHelper refreshTokenAuthHelper;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper,
            JwtService jwtService,
            RefreshTokenRepository refreshTokenRepository,
            TokenBlacklistService tokenBlacklistService,
            RefreshTokenAuthHelper refreshTokenAuthHelper
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenBlacklistService = tokenBlacklistService;
        this.refreshTokenAuthHelper = refreshTokenAuthHelper;
    }

    @Transactional
    public UserResponse register(RegisterRequest request){
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)){
            throw new EmailAlreadyExistsException(email);
        }

        User user = new User(
                email,
                passwordEncoder.encode(request.password()),
                request.fullName(),
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
        String refreshToken = refreshTokenAuthHelper.createAndSaveRefreshToken(user);

        return new LoginResponse(accessToken, refreshToken, userMapper.toResponse(user));
    }

    @Transactional
    public void logout(String accessToken, RefreshTokenRequest request){
        long expiryMillis = jwtService.extractExpiration(accessToken).getTime();
        tokenBlacklistService.blacklist(accessToken, expiryMillis);

        refreshTokenRepository.findByToken(request.refreshToken())
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request){
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new InvalidRefreshTokenException(
                        "Refresh token not found!"
                ));

        if (storedToken.isRevoked()){
            throw new InvalidRefreshTokenException("Refresh token has been revoked!");
        }

        if (storedToken.getExpiredAt().isBefore(LocalDateTime.now())){
            throw new InvalidRefreshTokenException("Refresh token has expired!");
        }

        User user = storedToken.getUser();

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = refreshTokenAuthHelper.createAndSaveRefreshToken(user);

        return new LoginResponse(newAccessToken, newRefreshToken, userMapper.toResponse(user));

    }
}
