package com.nexabank.auth.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

public class AuthDtos {

    @Data
    public static class RegisterRequest {
        @NotBlank @Size(min = 3, max = 50)
        private String username;

        @NotBlank @Email
        private String email;

        @NotBlank @Size(min = 8, max = 100)
        private String password;

        private String role = "ROLE_CUSTOMER";
    }

    @Data
    public static class LoginRequest {
        @NotBlank private String username;
        @NotBlank private String password;
    }

    @Data
    public static class RefreshRequest {
        @NotBlank private String refreshToken;
    }

    @Data
    public static class AuthResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType = "Bearer";
        private long expiresIn;
        private String username;
        private String role;
        private String email;

        public static AuthResponse of(String access, String refresh, long exp,
                                       String user, String role, String email) {
            AuthResponse r = new AuthResponse();
            r.accessToken = access; r.refreshToken = refresh;
            r.expiresIn = exp; r.username = user;
            r.role = role; r.email = email;
            return r;
        }
    }

    @Data
    public static class ApiError {
        private int status;
        private String message;
        private long timestamp = System.currentTimeMillis();

        public ApiError(int status, String message) {
            this.status = status; this.message = message;
        }
    }
}
