package com.nexabank.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankingChatbotService {

    private final ChatClient chatClient;
    private final InMemoryChatMemory chatMemory;

    private static final String BANKING_SYSTEM_PROMPT = """
        You are NexaBot, an intelligent AI banking assistant for NexaBank.
        
        Your capabilities:
        - Answer questions about account balances, transaction history, and banking products
        - Explain banking terms and policies in simple language
        - Guide users through processes (transfers, opening accounts, applying for loans)
        - Provide financial insights and spending analysis tips
        - Help with troubleshooting common banking issues
        
        Always be:
        - Professional, friendly, and concise
        - Proactive about security (never ask for full passwords or PINs)
        - Clear when you need to redirect to a human agent
        
        Current user context: {userContext}
        
        If you cannot help with something, clearly explain and offer to connect to a human agent.
        """;

    public String chat(String userMessage, String username, String conversationId) {
        log.info("AI chatbot query from user: {} conversation: {}", username, conversationId);

        String userContext = "Username: " + username;

        try {
            return chatClient.prompt()
                .system(s -> s.text(BANKING_SYSTEM_PROMPT)
                    .param("userContext", userContext))
                .user(userMessage)
                .advisors(a -> a.param("chat_memory_conversation_id", conversationId))
                .call()
                .content();

        } catch (Exception e) {
            log.error("Chatbot error for user {}: {}", username, e.getMessage());
            return "I'm sorry, I'm experiencing technical difficulties right now. "
                + "Please try again in a moment, or contact our support team at support@nexabank.com";
        }
    }

    public Flux<String> chatStream(String userMessage, String username, String conversationId) {
        String userContext = "Username: " + username;

        return chatClient.prompt()
            .system(s -> s.text(BANKING_SYSTEM_PROMPT).param("userContext", userContext))
            .user(userMessage)
            .advisors(a -> a.param("chat_memory_conversation_id", conversationId))
            .stream()
            .content();
    }

    public String generateSpendingInsights(String username, String transactionSummary) {
        String prompt = String.format("""
            Analyze this banking customer's recent transaction data and provide personalized financial insights.
            
            Customer: %s
            Transaction Summary:
            %s
            
            Please provide:
            1. Top 3 spending categories this month
            2. Notable changes vs last month
            3. 2-3 actionable saving tips
            4. Overall financial health score (1-10)
            
            Keep it friendly, specific, and under 300 words.
            """, username, transactionSummary);

        return chatClient.prompt().user(prompt).call().content();
    }
}
