package com.ecommerce.discountservice.config;

import com.ecommerce.discountservice.event.OrderCancelledEvent;
import com.ecommerce.discountservice.event.StockDecreasedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.DelegatingByTopicDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * TypeId 헤더 없이 JSON만 오는 메시지와 JsonSerializer(헤더 포함) 메시지 모두
 * 토픽당 단일 DTO로 역직렬화한다.
 */
@Configuration
public class KafkaConsumerConfig {

    @Bean
    @Primary
    public ConsumerFactory<String, Object> kafkaConsumerFactory(
            KafkaProperties kafkaProperties,
            ObjectMapper objectMapper) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties(null));
        props.remove(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG);
        props.remove(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG);

        Map<Pattern, Deserializer<?>> delegates = new LinkedHashMap<>();
        delegates.put(Pattern.compile("^stock-decreased$"),
                jsonDeserializer(StockDecreasedEvent.class, objectMapper));
        delegates.put(Pattern.compile("^order-cancelled$"),
                jsonDeserializer(OrderCancelledEvent.class, objectMapper));

        var valueDeserializer = new DelegatingByTopicDeserializer(delegates, new StringDeserializer());

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), valueDeserializer);
    }

    private static <T> JsonDeserializer<T> jsonDeserializer(Class<T> type, ObjectMapper objectMapper) {
        JsonDeserializer<T> d = new JsonDeserializer<>(type, objectMapper, false);
        d.addTrustedPackages("*");
        return d;
    }
}
