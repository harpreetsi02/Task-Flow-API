package com.backend.taskflowapi.security;

import com.backend.taskflowapi.entity.RefreshToken;
import com.backend.taskflowapi.entity.User;
import com.backend.taskflowapi.repository.RefreshTokenRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RefreshTokenAuthHelper {

    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenAuthHelper(
            JwtService jwtService,
            RefreshTokenRepository refreshTokenRepository
    ) {
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String createAndSaveRefreshToken(User user){
        String token = jwtService.generateAccessToken(user);

        RefreshToken refreshToken = new RefreshToken(
                token,
                user,
                LocalDateTime.now().plusDays(7)
        );

        refreshTokenRepository.save(refreshToken);
        return token;
    }
}
