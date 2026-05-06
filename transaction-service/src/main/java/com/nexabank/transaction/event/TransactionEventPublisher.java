package com.nexabank.transaction.event;

import com.nexabank.transaction.entity.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishTransactionCompleted(Transaction tx) {
        Map<String, Object> event = Map.of(
            "eventType", "TRANSACTION_COMPLETED",
            "transactionId", tx.getTransactionId(),
            "fromAccount", tx.getFromAccountNumber(),
            "toAccount", tx.getToAccountNumber(),
            "amount", tx.getAmount(),
            "currency", tx.getCurrency(),
            "username", tx.getUsername(),
            "reference", tx.getReferenceNumber(),
            "completedAt", tx.getCompletedAt().toString()
        );

        // Send to both notification and audit topics
        sendToTopic("transaction.events", tx.getTransactionId(), event);
        sendToTopic("audit.events", tx.getTransactionId(), event);
        sendToTopic("notification.events", tx.getTransactionId(), event);

        log.info("Published TRANSACTION_COMPLETED events for: {}", tx.getTransactionId());
    }

    public void publishTransactionFlagged(Transaction tx) {
        Map<String, Object> event = Map.of(
            "eventType", "TRANSACTION_FLAGGED",
            "transactionId", tx.getTransactionId(),
            "fromAccount", tx.getFromAccountNumber(),
            "amount", tx.getAmount(),
            "fraudScore", tx.getFraudScore(),
            "username", tx.getUsername()
        );
        sendToTopic("fraud.alerts", tx.getTransactionId(), event);
        sendToTopic("audit.events", tx.getTransactionId(), event);
        sendToTopic("notification.events", tx.getTransactionId(), event);
    }

    private void sendToTopic(String topic, String key, Object payload) {
        CompletableFuture<SendResult<String, Object>> future =
            kafkaTemplate.send(topic, key, payload);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send event to topic {}: {}", topic, ex.getMessage());
            } else {
                log.debug("Event sent to {} partition {} offset {}",
                    topic,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            }
        });
    }
}
