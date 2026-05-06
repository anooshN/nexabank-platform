package com.nexabank.audit.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "audit_logs")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {

    @Id
    private String id;

    @Indexed
    private String eventType;

    @Indexed
    private String transactionId;

    @Indexed
    private String username;

    private String fromAccount;
    private String toAccount;
    private String amount;
    private String currency;
    private String status;
    private String riskLevel;
    private Double fraudScore;

    private Map<String, Object> rawEvent;

    @Indexed
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    private String ipAddress;
    private String userAgent;

    // Audit file reference (GridFS)
    private String auditFileId;
}
