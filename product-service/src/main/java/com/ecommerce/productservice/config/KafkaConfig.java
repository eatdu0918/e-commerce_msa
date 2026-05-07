package com.ecommerce.productservice.config;

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

    public static final String TOPIC_STOCK_DECREASED = "stock-decreased";
    public static final String TOPIC_STOCK_DECREASE_FAILED = "stock-decrease-failed";

    /** Outbox String ??  ?   ???   ??   ??KafkaTemplate????      ??   ?   ????  ??    ??   ???  ?? */
    @Bean
    @Primary
    public KafkaTemplate<String, Object> kafkaTemplate(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties(null));
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }

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
