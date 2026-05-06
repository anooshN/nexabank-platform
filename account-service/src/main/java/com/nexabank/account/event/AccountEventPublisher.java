package com.nexabank.account.event;

import com.nexabank.account.entity.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishAccountCreated(Account account) {
        Map<String, Object> event = Map.of(
            "eventType", "ACCOUNT_CREATED",
            "accountNumber", account.getAccountNumber(),
            "username", account.getUsername(),
            "email", account.getEmail(),
            "accountType", account.getAccountType().name(),
            "timestamp", LocalDateTime.now().toString()
        );
        kafkaTemplate.send("account.events", account.getAccountNumber(), event);
        log.info("Published ACCOUNT_CREATED event for: {}", account.getAccountNumber());
    }

    public void publishAccountUpdated(Account account, String eventType) {
        Map<String, Object> event = Map.of(
            "eventType", eventType,
            "accountNumber", account.getAccountNumber(),
            "username", account.getUsername(),
            "status", account.getStatus().name(),
            "timestamp", LocalDateTime.now().toString()
        );
        kafkaTemplate.send("account.events", account.getAccountNumber(), event);
    }
}
