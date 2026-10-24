package com.backend.taskflowapi.security;

import com.backend.taskflowapi.dto.response.LoginResponse;
import com.backend.taskflowapi.entity.AuthProvider;
import com.backend.taskflowapi.entity.Role;
import com.backend.taskflowapi.entity.User;
import com.backend.taskflowapi.repository.UserRepository;
//import com.fasterxml.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenAuthHelper refreshTokenAuthHelper;
    private final ObjectMapper objectMapper;

    public OAuth2LoginSuccessHandler(
            JwtService jwtService,
            UserRepository userRepository,
            RefreshTokenAuthHelper refreshTokenAuthHelper,
            ObjectMapper objectMapper
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.refreshTokenAuthHelper = refreshTokenAuthHelper;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> registerNewGoogleUser(email, name));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenAuthHelper.createAndSaveRefreshToken(user);

        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(
                new LoginResponse(accessToken, refreshToken, null)
        ));
    }

    private User registerNewGoogleUser(String email, String name){
        User user = new User(
                email,
                null,
                name,
                Role.USER,
                AuthProvider.GOOGLE
        );

        return userRepository.save(user);
    }
}
