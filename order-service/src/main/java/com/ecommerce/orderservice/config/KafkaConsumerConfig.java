package com.ecommerce.orderservice.config;

import com.ecommerce.orderservice.event.CancelApprovedEvent;
import com.ecommerce.orderservice.event.CancelRejectedEvent;
import com.ecommerce.orderservice.event.CancelRequestedEvent;
import com.ecommerce.orderservice.event.CouponUseFailedEvent;
import com.ecommerce.orderservice.event.CouponUsedEvent;
import com.ecommerce.orderservice.event.PaymentCompletedEvent;
import com.ecommerce.orderservice.event.PaymentFailedEvent;
import com.ecommerce.orderservice.event.StockDecreaseFailedEvent;
import com.ecommerce.orderservice.event.StockDecreasedEvent;
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
 * ?      ???   ??TypeId ??   ??   JSON ?    ??   ?   ????? ?   ??   ?   
 * ?    ?????????   ?  ????? ?????  .
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
        delegates.put(Pattern.compile("^stock-decreased$"),
                jsonDeserializer(StockDecreasedEvent.class, objectMapper));
        delegates.put(Pattern.compile("^stock-decrease-failed$"),
                jsonDeserializer(StockDecreaseFailedEvent.class, objectMapper));
        delegates.put(Pattern.compile("^coupon-used$"),
                jsonDeserializer(CouponUsedEvent.class, objectMapper));
        delegates.put(Pattern.compile("^coupon-use-failed$"),
                jsonDeserializer(CouponUseFailedEvent.class, objectMapper));
        delegates.put(Pattern.compile("^payment-completed$"),
                jsonDeserializer(PaymentCompletedEvent.class, objectMapper));
        delegates.put(Pattern.compile("^payment-failed$"),
                jsonDeserializer(PaymentFailedEvent.class, objectMapper));
        delegates.put(Pattern.compile("^cancel-approved$"),
                jsonDeserializer(CancelApprovedEvent.class, objectMapper));
        delegates.put(Pattern.compile("^cancel-requested$"),
                jsonDeserializer(CancelRequestedEvent.class, objectMapper));
        delegates.put(Pattern.compile("^cancel-rejected$"),
                jsonDeserializer(CancelRejectedEvent.class, objectMapper));

        var valueDeserializer = new DelegatingByTopicDeserializer(delegates, new StringDeserializer());

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), valueDeserializer);
    }

    private static <T> JsonDeserializer<T> jsonDeserializer(Class<T> type, ObjectMapper objectMapper) {
        JsonDeserializer<T> d = new JsonDeserializer<>(type, objectMapper, false);
        d.addTrustedPackages("*");
        return d;
    }
}
