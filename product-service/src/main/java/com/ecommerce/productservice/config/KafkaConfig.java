package com.ecommerce.productservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_STOCK_DECREASED = "stock-decreased";
    public static final String TOPIC_STOCK_DECREASE_FAILED = "stock-decrease-failed";

    @Bean
    public NewTopic stockDecreasedTopic() {
        return TopicBuilder.name(TOPIC_STOCK_DECREASED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic stockDecreaseFailedTopic() {
        return TopicBuilder.name(TOPIC_STOCK_DECREASE_FAILED)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
