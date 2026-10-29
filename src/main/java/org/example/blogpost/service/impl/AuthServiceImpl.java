package org.example.blogpost.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.blogpost.dto.request.LoginRequest;
import org.example.blogpost.dto.request.RefreshTokenRequest;
import org.example.blogpost.dto.request.RegisterRequest;
import org.example.blogpost.dto.response.AuthResponse;
import org.example.blogpost.dto.response.UserResponse;
import org.example.blogpost.entities.User;
import org.example.blogpost.enums.Role;
import org.example.blogpost.exception.AppException;
import org.example.blogpost.exception.ErrorCode;
import org.example.blogpost.repositories.UserRepository;
import org.example.blogpost.service.AuthService;
import org.example.blogpost.service.RedisService;
import org.example.blogpost.service.TokenBlacklistService;
import org.example.blogpost.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthServiceImpl implements AuthService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    JwtUtil jwtUtil;
    RedisService redisService;
    TokenBlacklistService tokenBlacklistService;

    @Override
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .enabled(true)
                .build();

        userRepository.save(user);
        log.info("User registered: {}", user.getEmail());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // Kiểm tra email tồn tại không
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        // Kiểm tra password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }


        // Kiểm tra có bị kho tài khoản không
        if (!user.isEnabled()) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }

        log.info("User logged in: {}", user.getEmail());

        // Tạo token
        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtUtil.isRefreshToken(refreshToken) || tokenBlacklistService.isBlacklisted(refreshToken)) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        String email = jwtUtil.extractEmail(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!user.isEnabled()) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }

        if (!jwtUtil.isTokenValid(refreshToken)) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());

        log.info("Token refreshed for user: {}", email);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public UserResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        if (!jwtUtil.isAccessToken(accessToken)) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        // Refresh token đi kèm: phải cùng chủ với access token thì mới thu hồi.
        // Nếu nó đã hết hạn hoặc sai chữ ký thì vốn đã vô hiệu, bỏ qua.
        if (jwtUtil.isTokenValid(refreshToken)) {
            if (!jwtUtil.isRefreshToken(refreshToken)
                    || !jwtUtil.extractEmail(refreshToken).equals(jwtUtil.extractEmail(accessToken))) {
                throw new AppException(ErrorCode.INVALID_TOKEN);
            }
            tokenBlacklistService.blacklist(refreshToken);
        }

        tokenBlacklistService.blacklist(accessToken);
        log.info("User logged out: {}", jwtUtil.extractEmail(accessToken));
    }


}

