package com.nexabank.notification.config;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String NOTIFICATION_QUEUE = "nexabank.notifications";
    public static final String DLQ = "nexabank.notifications.dlq";
    public static final String EXCHANGE = "nexabank.exchange";
    public static final String DLX = "nexabank.dlx";

    @Bean public DirectExchange exchange() { return new DirectExchange(EXCHANGE); }
    @Bean public DirectExchange dlx() { return new DirectExchange(DLX); }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
            .withArgument("x-dead-letter-exchange", DLX)
            .withArgument("x-dead-letter-routing-key", DLQ)
            .withArgument("x-message-ttl", 60000)
            .build();
    }

    @Bean public Queue deadLetterQueue() { return QueueBuilder.durable(DLQ).build(); }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, DirectExchange exchange) {
        return BindingBuilder.bind(notificationQueue).to(exchange).with("notifications");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate t = new RabbitTemplate(cf);
        t.setMessageConverter(messageConverter());
        return t;
    }
}
