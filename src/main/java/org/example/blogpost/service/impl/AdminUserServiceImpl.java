package org.example.blogpost.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.blogpost.dto.response.AdminUserResponse;
import org.example.blogpost.entities.User;
import org.example.blogpost.enums.Role;
import org.example.blogpost.exception.AppException;
import org.example.blogpost.exception.ErrorCode;
import org.example.blogpost.repositories.UserRepository;
import org.example.blogpost.service.AdminUserService;
import org.example.blogpost.service.AuthUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    UserRepository userRepository;
    AuthUserService authUserService;

    @Override
    @Transactional
    public AdminUserResponse lockUser(Long userId, Long adminId) {
        User user = findUserOrThrow(userId);

        if (user.getRole() == Role.ADMIN) {
            throw new AppException(ErrorCode.CANNOT_LOCK_ADMIN);
        }

        user.setEnabled(false);
        User saved = userRepository.saveAndFlush(user);
        authUserService.evict(saved.getEmail());
        log.info("User locked: userId={}, by adminId={}", userId, adminId);
        return AdminUserResponse.from(saved);
    }

    @Override
    @Transactional
    public AdminUserResponse unlockUser(Long userId, Long adminId) {
        User user = findUserOrThrow(userId);
        user.setEnabled(true);
        User saved = userRepository.saveAndFlush(user);
        authUserService.evict(saved.getEmail());
        log.info("User unlocked: userId={}, by adminId={}", userId, adminId);
        return AdminUserResponse.from(saved);
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}