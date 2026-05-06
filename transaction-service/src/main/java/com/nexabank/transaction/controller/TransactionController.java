package com.nexabank.transaction.controller;

import com.nexabank.transaction.dto.TransactionDtos.*;
import com.nexabank.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Money movement operations")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    @Operation(summary = "Transfer money between accounts")
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransferRequest req,
            @RequestHeader("X-Auth-User") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(transactionService.transfer(req, username));
    }

    @PostMapping("/deposit")
    @Operation(summary = "Deposit money into an account")
    public ResponseEntity<TransactionResponse> deposit(
            @Valid @RequestBody DepositRequest req,
            @RequestHeader("X-Auth-User") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(transactionService.deposit(req, username));
    }

    @GetMapping("/{transactionId}")
    @Operation(summary = "Get a single transaction")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable String transactionId) {
        return ResponseEntity.ok(transactionService.getTransaction(transactionId));
    }

    @GetMapping("/account/{accountNumber}")
    @Operation(summary = "Get transaction history for an account")
    public ResponseEntity<Page<TransactionResponse>> getHistory(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(transactionService.getTransactionHistory(accountNumber, page, size));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all transactions (admin)")
    public ResponseEntity<Page<TransactionResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(transactionService.getAllTransactions(page, size));
    }
}
