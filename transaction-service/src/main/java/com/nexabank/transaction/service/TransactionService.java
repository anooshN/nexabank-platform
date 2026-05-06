package com.nexabank.transaction.service;

import com.nexabank.transaction.dto.TransactionDtos.*;
import com.nexabank.transaction.entity.Transaction;
import com.nexabank.transaction.event.TransactionEventPublisher;
import com.nexabank.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionEventPublisher eventPublisher;
    private final RestTemplate restTemplate;

    @Value("${services.account-service.url:http://localhost:8082}")
    private String accountServiceUrl;

    @Value("${services.ai-service.url:http://localhost:8086}")
    private String aiServiceUrl;

    @Transactional
    public TransactionResponse transfer(TransferRequest req, String username) {
        log.info("Processing transfer of {} {} from {} to {}",
            req.getAmount(), req.getCurrency(),
            req.getFromAccountNumber(), req.getToAccountNumber());

        // Create transaction record
        Transaction tx = Transaction.builder()
            .transactionId(UUID.randomUUID().toString())
            .fromAccountNumber(req.getFromAccountNumber())
            .toAccountNumber(req.getToAccountNumber())
            .username(username)
            .amount(req.getAmount())
            .currency(req.getCurrency() != null ? req.getCurrency() : "USD")
            .type(Transaction.TransactionType.TRANSFER)
            .status(Transaction.TransactionStatus.PENDING)
            .description(req.getDescription())
            .referenceNumber("TXN-" + System.currentTimeMillis())
            .build();

        tx = transactionRepository.save(tx);

        // AI Fraud check (async-friendly — non-blocking call)
        BigDecimal fraudScore = checkFraud(tx);
        tx.setFraudScore(fraudScore);

        if (fraudScore != null && fraudScore.compareTo(new BigDecimal("0.85")) > 0) {
            tx.setStatus(Transaction.TransactionStatus.FLAGGED);
            tx = transactionRepository.save(tx);
            eventPublisher.publishTransactionFlagged(tx);
            log.warn("Transaction FLAGGED for fraud: {} score={}", tx.getTransactionId(), fraudScore);
            throw new IllegalStateException("Transaction flagged for fraud review. Reference: " + tx.getReferenceNumber());
        }

        // Debit source account via account-service
        try {
            restTemplate.patchForObject(
                accountServiceUrl + "/api/accounts/" + req.getFromAccountNumber() + "/debit?amount=" + req.getAmount(),
                null, Void.class);
        } catch (Exception e) {
            tx.setStatus(Transaction.TransactionStatus.FAILED);
            transactionRepository.save(tx);
            throw new IllegalStateException("Failed to debit account: " + e.getMessage());
        }

        // Credit destination account
        try {
            restTemplate.patchForObject(
                accountServiceUrl + "/api/accounts/" + req.getToAccountNumber() + "/credit?amount=" + req.getAmount(),
                null, Void.class);
        } catch (Exception e) {
            // Compensating transaction — refund source
            restTemplate.patchForObject(
                accountServiceUrl + "/api/accounts/" + req.getFromAccountNumber() + "/credit?amount=" + req.getAmount(),
                null, Void.class);
            tx.setStatus(Transaction.TransactionStatus.FAILED);
            transactionRepository.save(tx);
            throw new IllegalStateException("Transfer failed and rolled back: " + e.getMessage());
        }

        tx.setStatus(Transaction.TransactionStatus.COMPLETED);
        tx.setCompletedAt(LocalDateTime.now());
        tx = transactionRepository.save(tx);

        // Publish events to Kafka (notification + audit)
        eventPublisher.publishTransactionCompleted(tx);

        log.info("Transfer COMPLETED: {} amount={}", tx.getTransactionId(), tx.getAmount());
        return mapToResponse(tx);
    }

    @Transactional
    public TransactionResponse deposit(DepositRequest req, String username) {
        Transaction tx = Transaction.builder()
            .transactionId(UUID.randomUUID().toString())
            .fromAccountNumber("EXTERNAL")
            .toAccountNumber(req.getAccountNumber())
            .username(username)
            .amount(req.getAmount())
            .currency(req.getCurrency() != null ? req.getCurrency() : "USD")
            .type(Transaction.TransactionType.DEPOSIT)
            .status(Transaction.TransactionStatus.COMPLETED)
            .description(req.getDescription())
            .referenceNumber("DEP-" + System.currentTimeMillis())
            .completedAt(LocalDateTime.now())
            .build();

        restTemplate.patchForObject(
            accountServiceUrl + "/api/accounts/" + req.getAccountNumber() + "/credit?amount=" + req.getAmount(),
            null, Void.class);

        tx = transactionRepository.save(tx);
        eventPublisher.publishTransactionCompleted(tx);
        return mapToResponse(tx);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionHistory(String accountNumber, int page, int size) {
        return transactionRepository
            .findByFromAccountNumberOrToAccountNumberOrderByCreatedAtDesc(
                accountNumber, accountNumber, PageRequest.of(page, size))
            .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAllTransactions(int page, int size) {
        return transactionRepository.findAll(
            PageRequest.of(page, size, Sort.by("createdAt").descending()))
            .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(String transactionId) {
        return transactionRepository.findByTransactionId(transactionId)
            .map(this::mapToResponse)
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));
    }

    private BigDecimal checkFraud(Transaction tx) {
        try {
            var response = restTemplate.postForObject(
                aiServiceUrl + "/api/ai/fraud/check",
                tx, java.util.Map.class);
            if (response != null && response.containsKey("fraudScore")) {
                return new BigDecimal(response.get("fraudScore").toString());
            }
        } catch (Exception e) {
            log.warn("AI fraud check unavailable, proceeding: {}", e.getMessage());
        }
        return null;
    }

    private TransactionResponse mapToResponse(Transaction t) {
        TransactionResponse r = new TransactionResponse();
        r.setId(t.getId()); r.setTransactionId(t.getTransactionId());
        r.setFromAccountNumber(t.getFromAccountNumber());
        r.setToAccountNumber(t.getToAccountNumber());
        r.setUsername(t.getUsername()); r.setAmount(t.getAmount());
        r.setCurrency(t.getCurrency()); r.setType(t.getType().name());
        r.setStatus(t.getStatus().name()); r.setDescription(t.getDescription());
        r.setReferenceNumber(t.getReferenceNumber());
        r.setFraudScore(t.getFraudScore());
        r.setCreatedAt(t.getCreatedAt()); r.setCompletedAt(t.getCompletedAt());
        return r;
    }
}
