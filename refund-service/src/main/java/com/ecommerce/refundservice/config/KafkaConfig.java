package com.ecommerce.refundservice.config;

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

    public static final String TOPIC_REFUND_COMPLETED = "refund-completed";
    public static final String TOPIC_REFUND_FAILED = "refund-failed";

    @Bean
    @Primary
    public KafkaTemplate<String, Object> kafkaTemplate(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties(null));
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }

    @Bean
    public NewTopic refundCompletedTopic() {
        return TopicBuilder.name(TOPIC_REFUND_COMPLETED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic refundFailedTopic() {
        return TopicBuilder.name(TOPIC_REFUND_FAILED)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
