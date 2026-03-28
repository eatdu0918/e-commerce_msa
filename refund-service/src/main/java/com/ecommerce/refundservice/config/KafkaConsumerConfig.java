package com.ecommerce.refundservice.config;

import com.ecommerce.refundservice.event.CancelApprovedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.DelegatingByTopicDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 아웃박스 등에서 TypeId 헤더 없이 JSON만 발행하는 프로듀서와 호환하기 위해
 * 토픽별 대상 타입을 고정해 역직렬화한다.
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
        props.keySet().removeIf(k -> k.startsWith("spring.json."));

        Map<Pattern, Deserializer<?>> delegates = new LinkedHashMap<>();
        delegates.put(Pattern.compile("^cancel-approved$"),
                jsonDeserializer(CancelApprovedEvent.class, objectMapper));

        var valueDeserializer = new DelegatingByTopicDeserializer(delegates, new StringDeserializer());

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), valueDeserializer);
    }

    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> kafkaConsumerFactory,
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, (ConsumerFactory<Object, Object>) (ConsumerFactory<?, ?>) kafkaConsumerFactory);
        return factory;
    }

    private static <T> JsonDeserializer<T> jsonDeserializer(Class<T> type, ObjectMapper objectMapper) {
        JsonDeserializer<T> d = new JsonDeserializer<>(type, objectMapper, false);
        d.addTrustedPackages("*");
        return d;
    }
}
