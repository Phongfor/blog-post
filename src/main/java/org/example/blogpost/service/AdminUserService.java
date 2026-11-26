package org.example.blogpost.service;

import org.example.blogpost.dto.response.AdminUserResponse;
import org.example.blogpost.dto.response.PageResponse;

public interface AdminUserService {

    PageResponse<AdminUserResponse> searchUsers(String search, int page, int size);

    AdminUserResponse lockUser(Long userId, Long adminId);

    AdminUserResponse unlockUser(Long userId, Long adminId);
}