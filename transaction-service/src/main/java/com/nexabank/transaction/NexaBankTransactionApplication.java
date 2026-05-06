package com.nexabank.transaction;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
@SpringBootApplication
@EnableKafka
public class NexaBankTransactionApplication {
    public static void main(String[] args) {
        SpringApplication.run(NexaBankTransactionApplication.class, args);
    }
}
