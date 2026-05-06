package com.nexabank.auth.service;

import com.nexabank.auth.dto.AuthDtos.*;
import com.nexabank.auth.entity.User;
import com.nexabank.auth.repository.UserRepository;
import com.nexabank.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = User.builder()
            .username(req.getUsername())
            .email(req.getEmail())
            .password(passwordEncoder.encode(req.getPassword()))
            .role(User.Role.valueOf(req.getRole()))
            .build();

        user = userRepository.save(user);
        log.info("New user registered: {}", user.getUsername());

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        User user = userRepository.findByUsername(req.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setLastLoginAt(LocalDateTime.now());

        AuthResponse response = buildAuthResponse(user);
        user.setRefreshToken(response.getRefreshToken());
        userRepository.save(user);

        // Cache token in Redis for fast invalidation
        redisTemplate.opsForValue().set(
            "token:" + user.getUsername(),
            response.getAccessToken(),
            24, TimeUnit.HOURS
        );

        log.info("User logged in: {}", user.getUsername());
        return response;
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest req) {
        User user = userRepository.findByRefreshToken(req.getRefreshToken())
            .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (!jwtUtil.isTokenValid(req.getRefreshToken(), user.getUsername())) {
            throw new IllegalArgumentException("Refresh token expired");
        }

        return buildAuthResponse(user);
    }

    @Transactional
    public void logout(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setRefreshToken(null);
            userRepository.save(user);
        });
        redisTemplate.delete("token:" + username);
        log.info("User logged out: {}", username);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        return AuthResponse.of(accessToken, refreshToken, 86400L,
            user.getUsername(), user.getRole().name(), user.getEmail());
    }
}
