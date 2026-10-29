package org.example.blogpost.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.blogpost.util.JwtUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TokenBlacklistService {

    private static final String PREFIX = "blacklist:";

    RedisService redisService;
    JwtUtil jwtUtil;

    public void blacklist(String token){
        long ttl = jwtUtil.getExpirationTime(token);

        if(ttl < 0){
            return;
        }
        redisService.set(PREFIX + jwtUtil.extractJti(token),"1",ttl);
    }

    public boolean isBlacklisted(String token) {
        return redisService.hasKey(PREFIX + jwtUtil.extractJti(token));
    }
}
