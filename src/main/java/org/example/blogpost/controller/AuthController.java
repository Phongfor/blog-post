package org.example.blogpost.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.blogpost.config.CustomUserDetails;
import org.example.blogpost.dto.request.RefreshTokenRequest;
import org.example.blogpost.dto.response.UserResponse;
import org.example.blogpost.service.AuthService;
import org.example.blogpost.dto.request.LoginRequest;
import org.example.blogpost.dto.request.RegisterRequest;
import org.example.blogpost.dto.response.ApiResponse;
import org.example.blogpost.dto.response.AuthResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    AuthService authService;

    @PostMapping("/register")
    public ApiResponse<Void> register(@RequestBody @Valid RegisterRequest request) {
        authService.register(request);
        return ApiResponse.<Void>builder()
                .message("Register successfully. Please login.")
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ApiResponse.<AuthResponse>builder()
                .result(authService.login(request))
                .message("Login successfully")
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        return ApiResponse.<AuthResponse>builder()
                .result(authService.refreshToken(request))
                .message("Token refreshed successfully")
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String authHeader,@RequestBody @Valid RefreshTokenRequest request){
        String accessToken = authHeader.substring(7);
        authService.logout(accessToken, request.getRefreshToken());
        return ApiResponse.<Void>builder()
                .message("Logout successfilly")
                .build();
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> getMe(@AuthenticationPrincipal CustomUserDetails principal) {
        return ApiResponse.<UserResponse>builder()
                .result(authService.getMe(principal.getId()))
                .message("Success")
                .build();
    }
}
