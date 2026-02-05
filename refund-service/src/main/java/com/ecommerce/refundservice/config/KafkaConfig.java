package com.ecommerce.refundservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_REFUND_COMPLETED = "refund-completed";
    public static final String TOPIC_REFUND_FAILED = "refund-failed";

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
