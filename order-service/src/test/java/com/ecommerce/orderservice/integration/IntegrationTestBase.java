package com.ecommerce.orderservice.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * 통합 테스트 기본 설정 클래스
 * H2 인메모리 DB + EmbeddedKafka를 사용하여 테스트
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
                "cancel-approved"
        },
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:0",
                "port=0"
        }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class IntegrationTestBase {

    // EmbeddedKafka의 bootstrap-servers는 application-integration-test.yml에서
    // ${spring.embedded.kafka.brokers}로 자동 주입됨
}
