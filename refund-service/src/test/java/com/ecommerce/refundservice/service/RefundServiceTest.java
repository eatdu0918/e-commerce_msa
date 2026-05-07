package com.ecommerce.refundservice.service;

import com.ecommerce.common.response.PageResponse;
import com.ecommerce.refundservice.dto.response.RefundResponse;
import com.ecommerce.refundservice.entity.Refund;
import com.ecommerce.refundservice.enums.RefundReason;
import com.ecommerce.refundservice.enums.RefundStatus;
import com.ecommerce.refundservice.exception.RefundDomainException;
import com.ecommerce.refundservice.kafka.RefundEventProducer;
import com.ecommerce.refundservice.repository.RefundRepository;
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
class RefundServiceTest {

    @Mock
    RefundRepository refundRepository;

    @Mock
    RefundEventProducer refundEventProducer;

    @InjectMocks
    RefundService refundService;

    private static final Long REFUND_ID = 1L;
    private static final Long ORDER_ID = 100L;
    private static final Long CANCEL_ID = 200L;
    private static final Long PAYMENT_ID = 300L;
    private static final Long USER_ID = 400L;
    private static final String REFUND_NUMBER = "REF-ABC123";

    private Refund testRefund;

    @BeforeEach
    void setUp() {
        testRefund = createTestRefund(REFUND_ID, RefundStatus.PENDING);
    }

    @Nested
    @DisplayName("??      ??)
    class GetRefundTest {

        @Test
        @DisplayName("??   ??      ???    (?????")
        void getRefund_success() {
            // given
            when(refundRepository.findByIdAndUserId(REFUND_ID, USER_ID))
                    .thenReturn(Optional.of(testRefund));

            // when
            RefundResponse response = refundService.getRefund(REFUND_ID, USER_ID);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(REFUND_ID);
            assertThat(response.getUserId()).isEqualTo(USER_ID);
        }

        @Test
        @DisplayName("??      ????   -    ???? ??  ")
        void getRefund_notFound_throwsException() {
            // given
            when(refundRepository.findByIdAndUserId(999L, USER_ID))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> refundService.getRefund(999L, USER_ID))
                    .isInstanceOf(RefundDomainException.class)
                    .hasMessageContaining("??  ??   ??????  ??  ");
        }

        @Test
        @DisplayName("????       ?   ??)
        void getMyRefunds_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Refund> refundPage = new PageImpl<>(List.of(testRefund), pageable, 1);

            when(refundRepository.findByUserId(USER_ID, pageable)).thenReturn(refundPage);

            // when
            PageResponse<RefundResponse> response = refundService.getMyRefunds(USER_ID, pageable);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("?  ?  ???      ???   ")
        void getRefundByCancelId_success() {
            // given
            when(refundRepository.findByCancelId(CANCEL_ID)).thenReturn(Optional.of(testRefund));

            // when
            RefundResponse response = refundService.getRefundByCancelId(CANCEL_ID, USER_ID);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getCancelId()).isEqualTo(CANCEL_ID);
        }

        @Test
        @DisplayName("?  ?  ???      ????   -    ???? ??  ")
        void getRefundByCancelId_notFound_throwsException() {
            // given
            when(refundRepository.findByCancelId(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> refundService.getRefundByCancelId(999L, USER_ID))
                    .isInstanceOf(RefundDomainException.class)
                    .hasMessageContaining("??  ??   ??????  ??  ");
        }

        @Test
        @DisplayName("?    ??       ?   ??)
        void getAllRefunds_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Refund> refundPage = new PageImpl<>(List.of(testRefund), pageable, 1);

            when(refundRepository.findAll(pageable)).thenReturn(refundPage);

            // when
            PageResponse<RefundResponse> response = refundService.getAllRefunds(pageable);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("?    ???       ?   ??)
        void getRefundsByStatus_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Refund> refundPage = new PageImpl<>(List.of(testRefund), pageable, 1);

            when(refundRepository.findByStatus(RefundStatus.PENDING, pageable)).thenReturn(refundPage);

            // when
            PageResponse<RefundResponse> response = refundService.getRefundsByStatus(RefundStatus.PENDING, pageable);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getStatus()).isEqualTo(RefundStatus.PENDING);
        }

        @Test
        @DisplayName("?  ?    ??   ??      ???   ")
        void getRefundById_success() {
            // given
            when(refundRepository.findById(REFUND_ID)).thenReturn(Optional.of(testRefund));

            // when
            RefundResponse response = refundService.getRefundById(REFUND_ID);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(REFUND_ID);
        }

        @Test
        @DisplayName("?  ?    ??      ????   -    ???? ??  ")
        void getRefundById_notFound_throwsException() {
            // given
            when(refundRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> refundService.getRefundById(999L))
                    .isInstanceOf(RefundDomainException.class)
                    .hasMessageContaining("??  ??   ??????  ??  ");
        }
    }

    @Nested
    @DisplayName("??   ?????)
    class RetryRefundTest {

        @Test
        @DisplayName("??   ??????   ")
        void retryRefund_success() {
            // given
            Refund failedRefund = createTestRefund(REFUND_ID, RefundStatus.FAILED);

            when(refundRepository.findById(REFUND_ID)).thenReturn(Optional.of(failedRefund));
            doNothing().when(refundEventProducer).sendRefundCompletedEvent(any());

            // when
            RefundResponse response = refundService.retryRefund(REFUND_ID);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(RefundStatus.COMPLETED);
            verify(refundEventProducer).sendRefundCompletedEvent(any());
        }

        @Test
        @DisplayName("??   ???????   -    ???? ??  ")
        void retryRefund_notFound_throwsException() {
            // given
            when(refundRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> refundService.retryRefund(999L))
                    .isInstanceOf(RefundDomainException.class)
                    .hasMessageContaining("??  ??   ??????  ??  ");
        }

        @Test
        @DisplayName("??   ???????   - ??????  ? ?   ")
        void retryRefund_invalidStatus_throwsException() {
            // given
            Refund completedRefund = createTestRefund(2L, RefundStatus.COMPLETED);

            when(refundRepository.findById(2L)).thenReturn(Optional.of(completedRefund));

            // when & then
            assertThatThrownBy(() -> refundService.retryRefund(2L))
                    .isInstanceOf(RefundDomainException.class)
                    .hasMessageContaining("?   ??? ??? ??   ?   ??  ??);
        }
    }

    @Nested
    @DisplayName("??   ?        ?)
    class UpdateRefundStatusTest {

        @Test
        @DisplayName("??   ?        ??   ")
        void updateRefundStatus_success() {
            // given
            when(refundRepository.findById(REFUND_ID)).thenReturn(Optional.of(testRefund));

            // when
            RefundResponse response = refundService.updateRefundStatus(REFUND_ID, RefundStatus.PROCESSING);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(RefundStatus.PROCESSING);
        }

        @Test
        @DisplayName("??   ?        ???   -    ???? ??  ")
        void updateRefundStatus_notFound_throwsException() {
            // given
            when(refundRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> refundService.updateRefundStatus(999L, RefundStatus.PROCESSING))
                    .isInstanceOf(RefundDomainException.class)
                    .hasMessageContaining("??  ??   ??????  ??  ");
        }
    }

    private Refund createTestRefund(Long id, RefundStatus status) {
        Refund refund = Refund.create(
                ORDER_ID, CANCEL_ID, PAYMENT_ID, USER_ID,
                RefundReason.ORDER_CANCEL, "     ?  ?  ??    ??  ",
                new BigDecimal("50000")
        );
        ReflectionTestUtils.setField(refund, "id", id);
        ReflectionTestUtils.setField(refund, "refundNumber", REFUND_NUMBER);
        ReflectionTestUtils.setField(refund, "status", status);
        return refund;
    }
}
