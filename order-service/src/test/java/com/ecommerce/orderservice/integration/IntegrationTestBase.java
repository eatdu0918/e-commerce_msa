package com.ecommerce.orderservice.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * ???? ??? ??   ????   ?? ???
 * H2 ?      ??DB + EmbeddedKafka???????   ??? ??
 */
@SpringBootTest
@ActiveProfiles("integration-test")
@Import(TestRedisConfig.class)
@EmbeddedKafka(
        partitions = 1,
        topics = {
                "order-created",
                "order-cancelled",
                "stock-decreased",
                "stock-decrease-failed",
                "coupon-used",
                "coupon-use-failed",
                "payment-failed",
                "payment-completed",
                "cancel-approved",
                "cancel-requested",
                "cancel-rejected"
        },
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:0",
                "port=0"
        }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class IntegrationTestBase {

    // EmbeddedKafka??bootstrap-servers??application-integration-test.yml? ? 
    // ${spring.embedded.kafka.brokers} ?? ?     ???
}
