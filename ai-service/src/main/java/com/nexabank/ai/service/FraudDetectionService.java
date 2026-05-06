package com.nexabank.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private final ChatClient chatClient;

    private static final String FRAUD_SYSTEM_PROMPT = """
        You are an expert AI fraud detection system for NexaBank.
        Analyze the following banking transaction and respond ONLY with a JSON object containing:
        {
          "fraudScore": <decimal 0.0-1.0>,
          "riskLevel": "<LOW|MEDIUM|HIGH|CRITICAL>",
          "flags": ["<reason1>", "<reason2>"],
          "recommendation": "<APPROVE|REVIEW|BLOCK>"
        }
        
        Risk factors to evaluate:
        - Unusual amount for account history
        - Suspicious timing (late night, rapid successive transactions)
        - New or unknown destination account
        - Geographic anomalies
        - Transaction velocity
        
        Respond ONLY with valid JSON. No explanation text.
        """;

    public FraudAnalysisResult analyzeFraud(FraudCheckRequest request) {
        log.info("Running AI fraud analysis for transaction: {}", request.getTransactionId());

        String transactionContext = buildTransactionContext(request);

        try {
            String response = chatClient.prompt()
                .system(FRAUD_SYSTEM_PROMPT)
                .user(transactionContext)
                .call()
                .content();

            return parseAiResponse(response, request.getTransactionId());

        } catch (Exception e) {
            log.error("AI fraud analysis failed for {}: {}", request.getTransactionId(), e.getMessage());
            // Fail-open: return low risk score on AI unavailability
            return FraudAnalysisResult.builder()
                .transactionId(request.getTransactionId())
                .fraudScore(BigDecimal.valueOf(0.05))
                .riskLevel("LOW")
                .recommendation("APPROVE")
                .flags(List.of("AI service temporarily unavailable"))
                .build();
        }
    }

    private String buildTransactionContext(FraudCheckRequest r) {
        return String.format("""
            Transaction Analysis Request:
            - Transaction ID: %s
            - Amount: %s %s
            - From Account: %s
            - To Account: %s
            - Transaction Type: %s
            - Timestamp: %s
            - Username: %s
            - Description: %s
            """,
            r.getTransactionId(), r.getAmount(), r.getCurrency(),
            r.getFromAccountNumber(), r.getToAccountNumber(),
            r.getType(), LocalDateTime.now(), r.getUsername(),
            r.getDescription() != null ? r.getDescription() : "N/A"
        );
    }

    private FraudAnalysisResult parseAiResponse(String jsonResponse, String transactionId) {
        try {
            // Basic JSON parsing — in production use Jackson ObjectMapper
            jsonResponse = jsonResponse.trim();
            if (jsonResponse.startsWith("```")) {
                jsonResponse = jsonResponse.replaceAll("```json", "").replaceAll("```", "").trim();
            }

            double score = extractJsonDouble(jsonResponse, "fraudScore");
            String riskLevel = extractJsonString(jsonResponse, "riskLevel");
            String recommendation = extractJsonString(jsonResponse, "recommendation");

            return FraudAnalysisResult.builder()
                .transactionId(transactionId)
                .fraudScore(BigDecimal.valueOf(score))
                .riskLevel(riskLevel)
                .recommendation(recommendation)
                .rawResponse(jsonResponse)
                .build();
        } catch (Exception e) {
            log.error("Failed to parse AI fraud response: {}", e.getMessage());
            return FraudAnalysisResult.builder()
                .transactionId(transactionId)
                .fraudScore(BigDecimal.valueOf(0.5))
                .riskLevel("MEDIUM")
                .recommendation("REVIEW")
                .build();
        }
    }

    private double extractJsonDouble(String json, String key) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx < 0) return 0.0;
        String sub = json.substring(idx + key.length() + 3).trim();
        String[] parts = sub.split("[,}]");
        return Double.parseDouble(parts[0].trim());
    }

    private String extractJsonString(String json, String key) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx < 0) return "UNKNOWN";
        String sub = json.substring(idx + key.length() + 3).trim();
        if (sub.startsWith("\"")) {
            return sub.substring(1, sub.indexOf("\"", 1));
        }
        return sub.split("[,}]")[0].trim();
    }

    // Inner DTOs
    @lombok.Data @lombok.Builder
    public static class FraudCheckRequest {
        private String transactionId;
        private String fromAccountNumber;
        private String toAccountNumber;
        private String username;
        private BigDecimal amount;
        private String currency;
        private String type;
        private String description;
    }

    @lombok.Data @lombok.Builder
    public static class FraudAnalysisResult {
        private String transactionId;
        private BigDecimal fraudScore;
        private String riskLevel;
        private String recommendation;
        private List<String> flags;
        private String rawResponse;
    }
}
