package com.blog.controller;

import com.blog.dto.AuthRequest;
import com.blog.dto.AuthResponse;
import com.blog.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(authService.register(
                new com.blog.entity.User(
                        null,
                        body.get("username"),
                        body.get("password"),
                        body.get("email"),
                        "ROLE_USER"
                )
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(authService.refreshAccessToken(body.get("refreshToken")));
    }
}
