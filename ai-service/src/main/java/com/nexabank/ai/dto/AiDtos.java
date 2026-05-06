package com.nexabank.ai.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AiDtos {
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ChatRequest {
        private String message;
        private String conversationId;
    }
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ChatResponse {
        private String response;
        private String conversationId;
    }
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class InsightRequest {
        private String transactionSummary;
    }
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class InsightResponse {
        private String insights;
    }
}
