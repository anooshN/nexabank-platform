package com.nexabank.audit.consumer;
import com.nexabank.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventConsumer {
    private final AuditService auditService;

    @KafkaListener(topics = "audit.events", groupId = "audit-service-group", concurrency = "3")
    public void handleAuditEvent(Map<String, Object> event) {
        log.info("Audit event received: type={} txId={}", event.get("eventType"), event.get("transactionId"));
        auditService.saveAuditLog(event);
    }

    @KafkaListener(topics = "fraud.alerts", groupId = "audit-fraud-group")
    public void handleFraudAlert(Map<String, Object> event) {
        log.warn("FRAUD alert received: txId={}", event.get("transactionId"));
        auditService.saveAuditLog(event);
    }

    @KafkaListener(topics = "account.events", groupId = "audit-account-group")
    public void handleAccountEvent(Map<String, Object> event) {
        log.info("Account event audited: {}", event.get("eventType"));
        auditService.saveAuditLog(event);
    }
}
