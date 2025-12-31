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

        String accessToken = jwtUtil.generateToken(user, 3600000); // 1h
        String refreshToken = jwtUtil.generateToken(user, 604800000); // 7 days

        return new AuthResponse(accessToken, refreshToken, user.getUsername());
    }

    @Override
    public User register(User user) {
        // 1. Check if username already exists to provide a clear error
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already taken");
        }

        // 2. Encode password
        user.setPassword(encoder.encode(user.getPassword()));
        
        // 3. Force default role regardless of what frontend sent
        user.setRole("ROLE_USER"); 
        
        return userRepository.save(user);
    }

    @Override
    public AuthResponse refreshAccessToken(String refreshToken) {

        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String username = jwtUtil.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtUtil.generateToken(user, 3600000);


        return new AuthResponse(newAccessToken, refreshToken, username);
    }
}
