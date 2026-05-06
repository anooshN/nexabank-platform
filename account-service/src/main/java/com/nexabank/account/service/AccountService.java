package com.nexabank.account.service;

import com.nexabank.account.dto.AccountDtos.*;
import com.nexabank.account.entity.Account;
import com.nexabank.account.event.AccountEventPublisher;
import com.nexabank.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountEventPublisher eventPublisher;

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest req) {
        if (accountRepository.existsByUsernameAndAccountType(
                req.getUsername(), Account.AccountType.valueOf(req.getAccountType()))) {
            throw new IllegalArgumentException("Account of this type already exists for user");
        }

        Account account = Account.builder()
            .accountNumber(generateAccountNumber())
            .username(req.getUsername())
            .fullName(req.getFullName())
            .email(req.getEmail())
            .accountType(Account.AccountType.valueOf(req.getAccountType()))
            .balance(req.getInitialDeposit() != null ? req.getInitialDeposit() : BigDecimal.ZERO)
            .availableBalance(req.getInitialDeposit() != null ? req.getInitialDeposit() : BigDecimal.ZERO)
            .currency(req.getCurrency() != null ? req.getCurrency() : "USD")
            .build();

        account = accountRepository.save(account);
        eventPublisher.publishAccountCreated(account);
        log.info("Account created: {} for user: {}", account.getAccountNumber(), account.getUsername());
        return mapToResponse(account);
    }

    @Cacheable(value = "accounts", key = "#accountNumber")
    @Transactional(readOnly = true)
    public AccountResponse getByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
            .map(this::mapToResponse)
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountNumber));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getByUsername(String username) {
        return accountRepository.findByUsername(username)
            .stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public Page<AccountResponse> getAllAccounts(int page, int size) {
        return accountRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))
            .map(this::mapToResponse);
    }

    @Transactional
    @CacheEvict(value = "accounts", key = "#accountNumber")
    public AccountResponse freezeAccount(String accountNumber) {
        Account account = getAccountEntity(accountNumber);
        account.setStatus(Account.AccountStatus.FROZEN);
        return mapToResponse(accountRepository.save(account));
    }

    @Transactional
    @CacheEvict(value = "accounts", key = "#accountNumber")
    public AccountResponse unfreezeAccount(String accountNumber) {
        Account account = getAccountEntity(accountNumber);
        account.setStatus(Account.AccountStatus.ACTIVE);
        return mapToResponse(accountRepository.save(account));
    }

    @Transactional
    public void debitAccount(String accountNumber, BigDecimal amount) {
        Account account = getAccountEntity(accountNumber);
        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active: " + accountNumber);
        }
        if (account.getAvailableBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds in account: " + accountNumber);
        }
        account.setBalance(account.getBalance().subtract(amount));
        account.setAvailableBalance(account.getAvailableBalance().subtract(amount));
        accountRepository.save(account);
        log.info("Debited {} from account {}", amount, accountNumber);
    }

    @Transactional
    public void creditAccount(String accountNumber, BigDecimal amount) {
        Account account = getAccountEntity(accountNumber);
        account.setBalance(account.getBalance().add(amount));
        account.setAvailableBalance(account.getAvailableBalance().add(amount));
        accountRepository.save(account);
        log.info("Credited {} to account {}", amount, accountNumber);
    }

    public Account getAccountEntity(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountNumber));
    }

    private String generateAccountNumber() {
        String prefix = "NXB";
        long timestamp = System.currentTimeMillis() % 1000000000L;
        int random = new Random().nextInt(9000) + 1000;
        return prefix + timestamp + random;
    }

    private AccountResponse mapToResponse(Account a) {
        AccountResponse r = new AccountResponse();
        r.setId(a.getId()); r.setAccountNumber(a.getAccountNumber());
        r.setUsername(a.getUsername()); r.setFullName(a.getFullName());
        r.setEmail(a.getEmail()); r.setAccountType(a.getAccountType().name());
        r.setBalance(a.getBalance()); r.setAvailableBalance(a.getAvailableBalance());
        r.setCurrency(a.getCurrency()); r.setStatus(a.getStatus().name());
        r.setCreatedAt(a.getCreatedAt());
        return r;
    }
}
