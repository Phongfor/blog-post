package org.example.blogpost.service;

public interface LoginAttemptService {
    void checkAllowed(String email, String ip);

    //Ghi nhận số lần đăng nhập sai
    void recordFailure(String email, String ip);

    void reset(String email);
}
