package org.example.blogpost.service;

import org.example.blogpost.dto.response.AdminUserResponse;

public interface AdminUserService {

    AdminUserResponse lockUser(Long userId, Long adminId);

    AdminUserResponse unlockUser(Long userId, Long adminId);
}