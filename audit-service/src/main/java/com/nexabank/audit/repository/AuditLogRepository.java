package com.nexabank.audit.repository;
import com.nexabank.audit.document.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
    List<AuditLog> findByUsernameOrderByTimestampDesc(String username);
    List<AuditLog> findByTransactionId(String transactionId);
    List<AuditLog> findByEventType(String eventType);
    List<AuditLog> findByUsernameAndTimestampBetween(String username, LocalDateTime from, LocalDateTime to);
    List<AuditLog> findByTimestampBetween(LocalDateTime from, LocalDateTime to);
}
