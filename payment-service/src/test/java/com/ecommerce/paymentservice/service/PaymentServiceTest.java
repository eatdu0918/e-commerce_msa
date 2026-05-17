package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.dto.request.CreatePaymentRequest;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.paymentservice.dto.response.PaymentResponse;
import com.ecommerce.paymentservice.entity.Payment;
import com.ecommerce.paymentservice.enums.PaymentMethod;
import com.ecommerce.paymentservice.enums.PaymentStatus;
import com.ecommerce.paymentservice.exception.PaymentDomainException;
import com.ecommerce.paymentservice.outbox.OutboxEventPublisher;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    OutboxEventPublisher outboxEventPublisher;

    @InjectMocks
    PaymentService paymentService;

    private static final Long PAYMENT_ID = 1L;
    private static final Long ORDER_ID = 100L;
    private static final Long USER_ID = 200L;
    private static final String ORDER_NUMBER = "ORD-ABC123";
    private static final BigDecimal AMOUNT = new BigDecimal("50000");

    private Payment testPayment;

    @BeforeEach
    void setUp() {
        testPayment = createTestPayment(PAYMENT_ID, ORDER_ID, USER_ID, PaymentStatus.PENDING);
    }

    @Nested
    @DisplayName("결제 생성")
    class CreatePaymentTest {

        @Test
        @DisplayName("결제 생성 성공")
        void createPayment_success() {
            // given
            CreatePaymentRequest request = new CreatePaymentRequest(
                    ORDER_ID, ORDER_NUMBER, PaymentMethod.CREDIT_CARD, AMOUNT, "카드 결제"
            );

            when(paymentRepository.findByOrderIdAndUserId(ORDER_ID, USER_ID)).thenReturn(Optional.empty());
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
                Payment payment = invocation.getArgument(0);
                ReflectionTestUtils.setField(payment, "id", PAYMENT_ID);
                return payment;
            });

            // when
            PaymentResponse response = paymentService.createPayment(USER_ID, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getOrderId()).isEqualTo(ORDER_ID);
            assertThat(response.getUserId()).isEqualTo(USER_ID);
            assertThat(response.getAmount()).isEqualByComparingTo(AMOUNT);
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(response.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
            verify(paymentRepository).save(any(Payment.class));
            verify(outboxEventPublisher).publishPaymentCompletedEvent(any());
        }

        @Test
        @DisplayName("동일 주문 결제 재요청 시 기존 건 반환 (멱등)")
        void createPayment_idempotent_returnsExisting() {
            Payment completed = createTestPayment(PAYMENT_ID, ORDER_ID, USER_ID, PaymentStatus.COMPLETED);
            CreatePaymentRequest request = new CreatePaymentRequest(
                    ORDER_ID, ORDER_NUMBER, PaymentMethod.CREDIT_CARD, AMOUNT, "카드 결제"
            );

            when(paymentRepository.findByOrderIdAndUserId(ORDER_ID, USER_ID))
                    .thenReturn(Optional.of(completed));

            PaymentResponse response = paymentService.createPayment(USER_ID, request);

            assertThat(response.getId()).isEqualTo(PAYMENT_ID);
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            verify(paymentRepository, never()).save(any(Payment.class));
            verify(outboxEventPublisher, never()).publishPaymentCompletedEvent(any());
        }
    }

    @Nested
    @DisplayName("결제 조회")
    class GetPaymentTest {

        @Test
        @DisplayName("결제 단건 조회 성공")
        void getPayment_success() {
            // given
            when(paymentRepository.findByIdAndUserId(PAYMENT_ID, USER_ID))
                    .thenReturn(Optional.of(testPayment));

            // when
            PaymentResponse response = paymentService.getPayment(PAYMENT_ID, USER_ID);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(PAYMENT_ID);
            assertThat(response.getOrderId()).isEqualTo(ORDER_ID);
        }

        @Test
        @DisplayName("결제 조회 실패 - 존재하지 않음")
        void getPayment_notFound_throwsException() {
            // given
            when(paymentRepository.findByIdAndUserId(999L, USER_ID))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> paymentService.getPayment(999L, USER_ID))
                    .isInstanceOf(PaymentDomainException.class)
                    .hasMessageContaining("결제를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("내 결제 목록 조회")
        void getMyPayments_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Payment> paymentPage = new PageImpl<>(List.of(testPayment), pageable, 1);

            when(paymentRepository.findByUserId(USER_ID, pageable)).thenReturn(paymentPage);

            // when
            PageResponse<PaymentResponse> response = paymentService.getMyPayments(USER_ID, pageable);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("주문 ID로 결제 조회 성공")
        void getPaymentByOrderId_success() {
            // given
            when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(testPayment));

            // when
            PaymentResponse response = paymentService.getPaymentByOrderId(ORDER_ID, USER_ID);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getOrderId()).isEqualTo(ORDER_ID);
        }

        @Test
        @DisplayName("주문 ID로 결제 조회 실패 - 없음")
        void getPaymentByOrderId_notFound_throwsException() {
            // given
            when(paymentRepository.findByOrderId(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> paymentService.getPaymentByOrderId(999L, USER_ID))
                    .isInstanceOf(PaymentDomainException.class)
                    .hasMessageContaining("결제를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("관리자 결제 조회")
    class AdminGetPaymentsTest {

        @Test
        @DisplayName("전체 결제 목록 조회")
        void getAllPayments_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Payment> paymentPage = new PageImpl<>(List.of(testPayment), pageable, 1);

            when(paymentRepository.findAll(pageable)).thenReturn(paymentPage);

            // when
            PageResponse<PaymentResponse> response = paymentService.getAllPayments(pageable);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("상태별 결제 목록 조회")
        void getPaymentsByStatus_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Payment completedPayment = createTestPayment(2L, 101L, USER_ID, PaymentStatus.COMPLETED);
            Page<Payment> paymentPage = new PageImpl<>(List.of(completedPayment), pageable, 1);

            when(paymentRepository.findByStatus(PaymentStatus.COMPLETED, pageable)).thenReturn(paymentPage);

            // when
            PageResponse<PaymentResponse> response = paymentService.getPaymentsByStatus(PaymentStatus.COMPLETED, pageable);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        }

        @Test
        @DisplayName("결제 ID로 조회 (관리자)")
        void getPaymentById_success() {
            // given
            when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(testPayment));

            // when
            PaymentResponse response = paymentService.getPaymentById(PAYMENT_ID);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(PAYMENT_ID);
        }

        @Test
        @DisplayName("결제 ID로 조회 실패 - 없음")
        void getPaymentById_notFound_throwsException() {
            // given
            when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> paymentService.getPaymentById(999L))
                    .isInstanceOf(PaymentDomainException.class)
                    .hasMessageContaining("결제를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("결제 상태 변경")
    class UpdatePaymentStatusTest {

        @Test
        @DisplayName("결제 상태 변경 성공 - COMPLETED")
        void updatePaymentStatus_toCompleted_success() {
            // given
            when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(testPayment));

            // when
            PaymentResponse response = paymentService.updatePaymentStatus(PAYMENT_ID, PaymentStatus.COMPLETED);

            // then
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        }

        @Test
        @DisplayName("결제 상태 변경 성공 - CANCELLED")
        void updatePaymentStatus_toCancelled_success() {
            // given
            when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(testPayment));

            // when
            PaymentResponse response = paymentService.updatePaymentStatus(PAYMENT_ID, PaymentStatus.CANCELLED);

            // then
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        }

        @Test
        @DisplayName("결제 상태 변경 성공 - REFUNDED")
        void updatePaymentStatus_toRefunded_success() {
            // given
            Payment completedPayment = createTestPayment(PAYMENT_ID, ORDER_ID, USER_ID, PaymentStatus.COMPLETED);
            when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(completedPayment));

            // when
            PaymentResponse response = paymentService.updatePaymentStatus(PAYMENT_ID, PaymentStatus.REFUNDED);

            // then
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        @DisplayName("결제 상태 변경 실패 - 결제 없음")
        void updatePaymentStatus_notFound_throwsException() {
            // given
            when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> paymentService.updatePaymentStatus(999L, PaymentStatus.COMPLETED))
                    .isInstanceOf(PaymentDomainException.class)
                    .hasMessageContaining("결제를 찾을 수 없습니다");
        }
    }

    private Payment createTestPayment(Long id, Long orderId, Long userId, PaymentStatus status) {
        Payment payment = Payment.create(orderId, ORDER_NUMBER, userId, PaymentMethod.CREDIT_CARD, AMOUNT, "카드 결제");
        ReflectionTestUtils.setField(payment, "id", id);
        ReflectionTestUtils.setField(payment, "status", status);
        ReflectionTestUtils.setField(payment, "paymentNumber", "PAY-TEST123456");
        return payment;
    }
}
