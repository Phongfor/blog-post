package org.example.blogpost.service.impl;

import jakarta.persistence.Access;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.blogpost.exception.AppException;
import org.example.blogpost.exception.ErrorCode;
import org.example.blogpost.service.LoginAttemptService;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Locale;

@Service
@Slf4j
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private static final String EMAIL_KEY_PREFIX = "login-fail:email:";
    private static final String IP_KEY_PREFIX = "login-fail:ip:";
    private static final int MAX_EMAIL_LENGTH = 100;

    private final RedissonClient redisson;
    private final int maxPerEmail;
    private final int maxPerIp;
    private final Duration window;

    public LoginAttemptServiceImpl(
            RedissonClient redisson,
            @Value("${app.rate-limit.login.max-attempts-per-email:5}") int maxPerEmail,
            @Value("${app.rate-limit.login.max-attempts-per-ip:20}") int maxPerIp,
            @Value("${app.rate-limit.login.window-seconds:900}") long windowSeconds) {
        this.redisson = redisson;
        this.maxPerEmail = maxPerEmail;
        this.maxPerIp = maxPerIp;
        this.window = Duration.ofSeconds(windowSeconds);
    }

    @Override
    public void checkAllowed(String email, String ip){
        boolean blocked = false;
        try {
            blocked = isBlocked(emailKey(email), maxPerEmail) || isBlocked(ipKey(ip), maxPerIp);
        }catch (RuntimeException e){
            log.error("Rate limiter unavailable (Redis), allowing login attempt", e);
        }

        if (blocked) {
            log.warn("Login blocked by rate limit: email={}, ip={}", normalizeEmail(email), ip);
            throw new AppException(ErrorCode.TOO_MANY_LOGIN_ATTEMPTS);
        }
    }

    @Override
    public void recordFailure(String email, String ip){
        try {
            limiter(emailKey(email), maxPerEmail).tryAcquire();
            limiter(ipKey(ip), maxPerIp).tryAcquire();
        } catch (RuntimeException e) {
            log.error("Cannot record failed login (Redis unavailable)", e);
        }
    }

    @Override
    public void reset(String email){
        try {
            redisson.getRateLimiter(emailKey(email)).delete();
        } catch (RuntimeException e) {
            log.error("Cannot reset login counter (Redis unavailable)", e);
        }
    }

    private RRateLimiter limiter(String key, int max) {
        RRateLimiter limiter = redisson.getRateLimiter(key);
        limiter.trySetRate(RateType.OVERALL, max, window, window);
        return limiter;
    }

    private boolean isBlocked(String key, int max) {
        RRateLimiter limiter = redisson.getRateLimiter(key);
        return limiter.isExists() && limiter.availablePermits() <= 0;
    }

    private String emailKey(String email) {
        return EMAIL_KEY_PREFIX + normalizeEmail(email);
    }

    private String ipKey(String ip) {
        return IP_KEY_PREFIX + (ip == null || ip.isBlank() ? "unknown" : ip);
    }

    private String normalizeEmail(String email) {
        String normalized = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        return normalized.length() > MAX_EMAIL_LENGTH ? normalized.substring(0, MAX_EMAIL_LENGTH) : normalized;
    }
}
