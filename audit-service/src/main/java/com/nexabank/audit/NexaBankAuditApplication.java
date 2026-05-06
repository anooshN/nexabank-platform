package com.nexabank.audit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
@SpringBootApplication @EnableKafka
public class NexaBankAuditApplication {
    public static void main(String[] args) { SpringApplication.run(NexaBankAuditApplication.class, args); }
}
