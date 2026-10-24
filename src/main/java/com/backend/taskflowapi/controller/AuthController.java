package com.backend.taskflowapi.controller;

import com.backend.taskflowapi.dto.request.LoginRequest;
import com.backend.taskflowapi.dto.request.RefreshTokenRequest;
import com.backend.taskflowapi.dto.request.RegisterRequest;
import com.backend.taskflowapi.dto.response.LoginResponse;
import com.backend.taskflowapi.dto.response.UserResponse;
import com.backend.taskflowapi.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        UserResponse response =
                authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response =
                authService.login(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        String accessToken = authHeader.substring(7);
        authService.logout(accessToken, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        LoginResponse response =
                authService.refreshToken(request);

        return ResponseEntity.ok(response);
    }
}
