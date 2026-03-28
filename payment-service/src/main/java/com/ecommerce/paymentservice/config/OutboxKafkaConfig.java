package com.ecommerce.paymentservice.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 아웃박스 페이로드는 이미 JSON 문자열이다. 기본 producer가 JsonSerializer이면
 * 값이 JSON 문자열로 한 번 더 인코딩되어 소비 측 Json 역직렬화가 실패한다.
 * 아웃박스 릴레이 전용으로 원시 UTF-8 JSON 바이트를 보낸다.
 */
@Configuration
public class OutboxKafkaConfig {

    public static final String OUTBOX_KAFKA_TEMPLATE = "outboxKafkaTemplate";

    @Bean(name = OUTBOX_KAFKA_TEMPLATE)
    public KafkaTemplate<String, String> outboxKafkaTemplate(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }
}
