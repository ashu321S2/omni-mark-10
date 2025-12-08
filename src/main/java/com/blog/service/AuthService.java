package com.blog.service;

import com.blog.dto.AuthRequest;
import com.blog.dto.AuthResponse;
import com.blog.entity.User;

public interface AuthService {

    AuthResponse login(AuthRequest request);

    User register(User user);

    AuthResponse refreshAccessToken(String refreshToken);
}
