package com.ecommerce.cancelservice.config;

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

    public static final String TOPIC_CANCEL_REQUESTED = "cancel-requested";
    public static final String TOPIC_CANCEL_APPROVED = "cancel-approved";
    public static final String TOPIC_CANCEL_REJECTED = "cancel-rejected";

    @Bean
    @Primary
    public KafkaTemplate<String, Object> kafkaTemplate(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties(null));
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }

    @Bean
    public NewTopic cancelRequestedTopic() {
        return TopicBuilder.name(TOPIC_CANCEL_REQUESTED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic cancelApprovedTopic() {
        return TopicBuilder.name(TOPIC_CANCEL_APPROVED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic cancelRejectedTopic() {
        return TopicBuilder.name(TOPIC_CANCEL_REJECTED)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
