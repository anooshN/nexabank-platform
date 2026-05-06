package com.nexabank.transaction.repository;

import com.nexabank.transaction.entity.Transaction;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTransactionId(String transactionId);

    Page<Transaction> findByFromAccountNumberOrToAccountNumberOrderByCreatedAtDesc(
        String from, String to, Pageable pageable);

    Page<Transaction> findByUsernameOrderByCreatedAtDesc(String username, Pageable pageable);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.createdAt >= :since")
    long countTransactionsSince(LocalDateTime since);

    @Query("SELECT t FROM Transaction t WHERE t.fraudScore >= :minScore ORDER BY t.fraudScore DESC")
    Page<Transaction> findFlaggedTransactions(double minScore, Pageable pageable);
}
