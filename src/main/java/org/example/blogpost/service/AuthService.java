package org.example.blogpost.service;

import org.example.blogpost.dto.request.LoginRequest;
import org.example.blogpost.dto.request.RefreshTokenRequest;
import org.example.blogpost.dto.request.RegisterRequest;
import org.example.blogpost.dto.response.AuthResponse;
import org.example.blogpost.dto.response.UserResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    void register(RegisterRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    UserResponse getMe(Long userId);

//    void logout(String accessToken);
}
