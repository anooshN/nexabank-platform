package com.nexabank.account.repository;

import com.nexabank.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findByUsername(String username);
    boolean existsByUsernameAndAccountType(String username, Account.AccountType accountType);

    @Query("SELECT a FROM Account a WHERE a.status = 'ACTIVE' ORDER BY a.balance DESC")
    List<Account> findAllActiveAccounts();
}
