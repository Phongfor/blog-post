package org.example.blogpost.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.blogpost.config.CustomUserDetails;
import org.example.blogpost.dto.response.AdminUserResponse;
import org.example.blogpost.dto.response.ApiResponse;
import org.example.blogpost.service.AdminUserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminUserController {

    AdminUserService adminUserService;

    @PutMapping("/{id}/lock")
    public ApiResponse<AdminUserResponse> lockUser(@AuthenticationPrincipal CustomUserDetails principal,
                                                   @PathVariable Long id) {
        return ApiResponse.<AdminUserResponse>builder()
                .message("User locked successfully")
                .result(adminUserService.lockUser(id, principal.getId()))
                .build();
    }

    @PutMapping("/{id}/unlock")
    public ApiResponse<AdminUserResponse> unlockUser(@AuthenticationPrincipal CustomUserDetails principal,
                                                     @PathVariable Long id) {
        return ApiResponse.<AdminUserResponse>builder()
                .message("User unlocked successfully")
                .result(adminUserService.unlockUser(id, principal.getId()))
                .build();
    }
}