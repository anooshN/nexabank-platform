package com.nexabank.audit.service;

import com.nexabank.audit.document.AuditLog;
import com.nexabank.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final MongoTemplate mongoTemplate;

    public AuditLog saveAuditLog(Map<String, Object> event) {
        AuditLog log = AuditLog.builder()
            .eventType(String.valueOf(event.getOrDefault("eventType", "UNKNOWN")))
            .transactionId(String.valueOf(event.getOrDefault("transactionId", "")))
            .username(String.valueOf(event.getOrDefault("username", "")))
            .fromAccount(String.valueOf(event.getOrDefault("fromAccount", "")))
            .toAccount(String.valueOf(event.getOrDefault("toAccount", "")))
            .amount(String.valueOf(event.getOrDefault("amount", "")))
            .currency(String.valueOf(event.getOrDefault("currency", "USD")))
            .status(String.valueOf(event.getOrDefault("status", "COMPLETED")))
            .rawEvent(event)
            .timestamp(LocalDateTime.now())
            .build();

        return auditLogRepository.save(log);
    }

    public Page<AuditLog> getAuditLogs(int page, int size) {
        return auditLogRepository.findAll(
            PageRequest.of(page, size, Sort.by("timestamp").descending()));
    }

    public List<AuditLog> getAuditLogsByUsername(String username) {
        return auditLogRepository.findByUsernameOrderByTimestampDesc(username);
    }

    public List<AuditLog> getAuditLogsByTransactionId(String transactionId) {
        return auditLogRepository.findByTransactionId(transactionId);
    }

    public byte[] exportAuditLogsToCsv(String username, LocalDateTime from, LocalDateTime to) {
        List<AuditLog> logs = username != null
            ? auditLogRepository.findByUsernameAndTimestampBetween(username, from, to)
            : auditLogRepository.findByTimestampBetween(from, to);

        StringBuilder csv = new StringBuilder();
        csv.append("Timestamp,EventType,TransactionId,Username,FromAccount,ToAccount,Amount,Currency,Status\n");

        for (AuditLog al : logs) {
            csv.append(String.join(",",
                safe(al.getTimestamp() != null ? al.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : ""),
                safe(al.getEventType()),
                safe(al.getTransactionId()),
                safe(al.getUsername()),
                safe(al.getFromAccount()),
                safe(al.getToAccount()),
                safe(al.getAmount()),
                safe(al.getCurrency()),
                safe(al.getStatus())
            )).append("\n");
        }

        return csv.toString().getBytes();
    }

    private String safe(String s) {
        return s == null ? "" : "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
