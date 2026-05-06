package com.nexabank.ai.controller;

import com.nexabank.ai.dto.AiDtos.*;
import com.nexabank.ai.service.BankingChatbotService;
import com.nexabank.ai.service.FraudDetectionService;
import com.nexabank.ai.service.FraudDetectionService.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI Services", description = "Spring AI powered fraud detection and chatbot")
public class AiController {

    private final FraudDetectionService fraudDetectionService;
    private final BankingChatbotService chatbotService;

    @PostMapping("/fraud/check")
    @Operation(summary = "Check transaction for fraud using AI")
    public ResponseEntity<FraudAnalysisResult> checkFraud(@RequestBody FraudCheckRequest req) {
        return ResponseEntity.ok(fraudDetectionService.analyzeFraud(req));
    }

    @PostMapping("/chat")
    @Operation(summary = "Chat with NexaBot banking AI assistant")
    public ResponseEntity<ChatResponse> chat(
            @RequestBody ChatRequest req,
            @RequestHeader("X-Auth-User") String username) {
        String response = chatbotService.chat(req.getMessage(), username, req.getConversationId());
        return ResponseEntity.ok(new ChatResponse(response, req.getConversationId()));
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream chat response from NexaBot")
    public Flux<String> chatStream(
            @RequestBody ChatRequest req,
            @RequestHeader("X-Auth-User") String username) {
        return chatbotService.chatStream(req.getMessage(), username, req.getConversationId());
    }

    @PostMapping("/insights")
    @Operation(summary = "Generate AI spending insights for a user")
    public ResponseEntity<InsightResponse> getInsights(
            @RequestBody InsightRequest req,
            @RequestHeader("X-Auth-User") String username) {
        String insights = chatbotService.generateSpendingInsights(username, req.getTransactionSummary());
        return ResponseEntity.ok(new InsightResponse(insights));
    }
}
