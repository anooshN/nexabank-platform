package com.nexabank.account.controller;

import com.nexabank.account.dto.AccountDtos.*;
import com.nexabank.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Bank account management")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Open a new bank account")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(req));
    }

    @GetMapping("/{accountNumber}")
    @Operation(summary = "Get account by account number")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getByAccountNumber(accountNumber));
    }

    @GetMapping("/user/{username}")
    @Operation(summary = "Get all accounts for a user")
    public ResponseEntity<List<AccountResponse>> getUserAccounts(@PathVariable String username) {
        return ResponseEntity.ok(accountService.getByUsername(username));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all accounts (admin only)")
    public ResponseEntity<Page<AccountResponse>> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(accountService.getAllAccounts(page, size));
    }

    @PatchMapping("/{accountNumber}/freeze")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Freeze an account")
    public ResponseEntity<AccountResponse> freezeAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.freezeAccount(accountNumber));
    }

    @PatchMapping("/{accountNumber}/unfreeze")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Unfreeze an account")
    public ResponseEntity<AccountResponse> unfreezeAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.unfreezeAccount(accountNumber));
    }
}
// (These exist in the file — adding internal endpoints as separate mappings)
