package com.nexabank.account.controller;

import com.nexabank.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

/**
 * Internal endpoints called only by other microservices via API Gateway
 * Not exposed publicly — protected by gateway + internal network
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class InternalAccountController {

    private final AccountService accountService;

    @PatchMapping("/{accountNumber}/debit")
    public ResponseEntity<Void> debit(@PathVariable String accountNumber,
                                       @RequestParam BigDecimal amount) {
        accountService.debitAccount(accountNumber, amount);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{accountNumber}/credit")
    public ResponseEntity<Void> credit(@PathVariable String accountNumber,
                                        @RequestParam BigDecimal amount) {
        accountService.creditAccount(accountNumber, amount);
        return ResponseEntity.ok().build();
    }
}
