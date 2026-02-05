package com.ecommerce.cancelservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_CANCEL_REQUESTED = "cancel-requested";
    public static final String TOPIC_CANCEL_APPROVED = "cancel-approved";
    public static final String TOPIC_CANCEL_REJECTED = "cancel-rejected";

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
