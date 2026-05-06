package com.nexabank.notification.consumer;

import com.nexabank.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final EmailService emailService;

    // Kafka consumer for transaction events
    @KafkaListener(
        topics = "notification.events",
        groupId = "notification-service-group",
        concurrency = "3"
    )
    public void handleTransactionEvent(Map<String, Object> event) {
        log.info("Received notification event: {}", event.get("eventType"));

        String eventType = String.valueOf(event.get("eventType"));

        switch (eventType) {
            case "TRANSACTION_COMPLETED" -> emailService.sendTransactionConfirmation(event);
            case "ACCOUNT_CREATED" -> emailService.sendWelcomeEmail(event);
            case "TRANSACTION_FLAGGED" -> emailService.sendFraudAlert(event);
            default -> log.warn("Unknown event type: {}", eventType);
        }
    }

    // RabbitMQ consumer for priority notifications
    @RabbitListener(queues = "${rabbitmq.queues.notifications}")
    public void handleRabbitNotification(Map<String, Object> message) {
        log.info("Received RabbitMQ notification: {}", message.get("type"));
        String type = String.valueOf(message.get("type"));

        switch (type) {
            case "PASSWORD_RESET" -> emailService.sendPasswordReset(message);
            case "ACCOUNT_LOCKED" -> emailService.sendAccountLocked(message);
            case "LARGE_TRANSACTION_ALERT" -> emailService.sendLargeTransactionAlert(message);
            default -> log.warn("Unknown RabbitMQ message type: {}", type);
        }
    }
}
