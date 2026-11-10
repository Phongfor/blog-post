package org.example.blogpost.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.blogpost.config.AppCacheConfig;
import org.example.blogpost.dto.cache.AuthUser;
import org.example.blogpost.repositories.UserRepository;
import org.example.blogpost.service.AuthUserService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED,makeFinal = true)
public class AuthUserServiceImpl implements AuthUserService {

    UserRepository userRepository;

    @Override
    @Cacheable(cacheNames = AppCacheConfig.AUTH_USER, key = "#email")
    public AuthUser getByEmail(String email){
        return userRepository.findByEmail(email)
                .map(AuthUser :: from)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    @CacheEvict(cacheNames = AppCacheConfig.AUTH_USER,key = "#email")
    public void evict(String email){}

}
