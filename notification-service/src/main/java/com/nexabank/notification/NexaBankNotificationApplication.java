package com.nexabank.notification;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
@SpringBootApplication @EnableKafka @EnableAsync
public class NexaBankNotificationApplication {
    public static void main(String[] args) {
        SpringApplication.run(NexaBankNotificationApplication.class, args);
    }
}
