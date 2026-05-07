package com.ecommerce.paymentservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_PAYMENT_COMPLETED = "payment-completed";
    public static final String TOPIC_PAYMENT_FAILED = "payment-failed";
    public static final String TOPIC_PAYMENT_CANCELLED = "payment-cancelled";

    /** Outbox String ??  ?   ???   ??   ??KafkaTemplate????      ??   ?   ????  ??    ??   ???  ?? */
    @Bean
    @Primary
    public KafkaTemplate<String, Object> kafkaTemplate(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties(null));
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }

    @Bean
    public NewTopic paymentCompletedTopic() {
        return TopicBuilder.name(TOPIC_PAYMENT_COMPLETED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentFailedTopic() {
        return TopicBuilder.name(TOPIC_PAYMENT_FAILED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentCancelledTopic() {
        return TopicBuilder.name(TOPIC_PAYMENT_CANCELLED)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
