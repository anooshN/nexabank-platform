package com.nexabank.account.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountDtos {

    @Data
    public static class CreateAccountRequest {
        @NotBlank private String username;
        @NotBlank private String fullName;
        @NotBlank @Email private String email;
        @NotBlank private String accountType;
        private BigDecimal initialDeposit;
        private String currency;
    }

    @Data
    public static class AccountResponse {
        private Long id;
        private String accountNumber;
        private String username;
        private String fullName;
        private String email;
        private String accountType;
        private BigDecimal balance;
        private BigDecimal availableBalance;
        private String currency;
        private String status;
        private LocalDateTime createdAt;
    }

    @Data
    public static class BalanceResponse {
        private String accountNumber;
        private BigDecimal balance;
        private BigDecimal availableBalance;
        private String currency;
    }
}
