package org.example.blogpost.service;

import org.example.blogpost.dto.cache.AuthUser;

public interface AuthUserService {
    AuthUser getByEmail(String email);

    void evict(String emmail);
}
