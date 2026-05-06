package com.nexabank.ai.config;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {
    @Bean
    public InMemoryChatMemory chatMemory() { return new InMemoryChatMemory(); }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, InMemoryChatMemory memory) {
        return builder
            .defaultAdvisors(new MessageChatMemoryAdvisor(memory))
            .build();
    }
}
