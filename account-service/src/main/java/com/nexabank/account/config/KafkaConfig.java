package com.nexabank.account.config;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
@Configuration
public class KafkaConfig {
    @Bean public NewTopic accountEvents() { return TopicBuilder.name("account.events").partitions(6).replicas(1).build(); }
}
