package com.blog.service;

import com.blog.dto.AuthRequest;
import com.blog.dto.AuthResponse;
import com.blog.entity.User;
import com.blog.repository.UserRepository;
import com.blog.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponse login(AuthRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String accessToken = jwtUtil.generateToken(user.getUsername(), 3600000); // 1h
        String refreshToken = jwtUtil.generateToken(user.getUsername(), 604800000); // 7 days

        return new AuthResponse(accessToken, refreshToken, user.getUsername());
    }

    @Override
    public User register(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        if (user.getRole() == null) user.setRole("ROLE_USER");
        return userRepository.save(user);
    }

    @Override
    public AuthResponse refreshAccessToken(String refreshToken) {

        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String username = jwtUtil.getUsernameFromToken(refreshToken);

        String newAccessToken = jwtUtil.generateToken(username, 3600000); // 1 hour

        return new AuthResponse(newAccessToken, refreshToken, username);
    }
}
