package com.nexabank.transaction.config;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
@Configuration
public class KafkaConfig {
    @Bean public NewTopic transactionEvents() { return TopicBuilder.name("transaction.events").partitions(12).replicas(1).build(); }
    @Bean public NewTopic auditEvents() { return TopicBuilder.name("audit.events").partitions(6).replicas(1).build(); }
    @Bean public NewTopic notificationEvents() { return TopicBuilder.name("notification.events").partitions(6).replicas(1).build(); }
    @Bean public NewTopic fraudAlerts() { return TopicBuilder.name("fraud.alerts").partitions(3).replicas(1).build(); }
    @Bean public NewTopic accountEvents() { return TopicBuilder.name("account.events").partitions(6).replicas(1).build(); }
}
