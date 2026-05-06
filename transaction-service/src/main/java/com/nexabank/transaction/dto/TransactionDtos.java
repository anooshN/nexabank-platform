package com.nexabank.transaction.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDtos {

    @Data
    public static class TransferRequest {
        @NotBlank private String fromAccountNumber;
        @NotBlank private String toAccountNumber;
        @NotNull @DecimalMin("0.01") private BigDecimal amount;
        private String currency;
        private String description;
    }

    @Data
    public static class DepositRequest {
        @NotBlank private String accountNumber;
        @NotNull @DecimalMin("0.01") private BigDecimal amount;
        private String currency;
        private String description;
    }

    @Data
    public static class TransactionResponse {
        private Long id;
        private String transactionId;
        private String fromAccountNumber;
        private String toAccountNumber;
        private String username;
        private BigDecimal amount;
        private String currency;
        private String type;
        private String status;
        private String description;
        private String referenceNumber;
        private BigDecimal fraudScore;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;
    }
}
