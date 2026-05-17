package com.ecommerce.cancelservice.service;

import com.ecommerce.cancelservice.dto.request.CancelItemRequest;
import com.ecommerce.cancelservice.dto.request.CreateCancelRequest;
import com.ecommerce.cancelservice.client.OrderServiceClient;
import com.ecommerce.cancelservice.client.dto.OrderPayload;
import com.ecommerce.cancelservice.dto.response.CancelResponse;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.cancelservice.entity.Cancel;
import com.ecommerce.cancelservice.entity.CancelItem;
import com.ecommerce.cancelservice.enums.CancelReason;
import com.ecommerce.cancelservice.enums.CancelRequestType;
import com.ecommerce.cancelservice.enums.CancelStatus;
import com.ecommerce.cancelservice.event.CancelApprovedEvent;
import com.ecommerce.cancelservice.exception.CancelDomainException;
import com.ecommerce.cancelservice.outbox.OutboxEventPublisher;
import com.ecommerce.cancelservice.repository.CancelRepository;
import com.ecommerce.common.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelServiceTest {

    @Mock
    CancelRepository cancelRepository;

    @Mock
    OutboxEventPublisher outboxEventPublisher;

    @Mock
    OrderServiceClient orderServiceClient;

    @InjectMocks
    CancelService cancelService;

    private static final Long CANCEL_ID = 1L;
    private static final Long ORDER_ID = 100L;
    private static final Long USER_ID = 200L;
    private static final String ORDER_NUMBER = "ORD-TEST123";
    private static final String CANCEL_NUMBER = "CAN-ABC123";

    private Cancel testCancel;

    @BeforeEach
    void setUp() {
        testCancel = createTestCancel(CANCEL_ID, ORDER_ID, USER_ID, ORDER_NUMBER, CancelStatus.REQUESTED);
    }

    @Nested
    @DisplayName("취소 요청 생성")
    class CreateCancelTest {

        @Test
        @DisplayName("취소 요청 생성 성공")
        void createCancel_success() {
            // given
            CancelItemRequest itemRequest = new CancelItemRequest(
                    1L, "테스트 상품", 2, new BigDecimal("10000")
            );
            CreateCancelRequest request = new CreateCancelRequest(
                    ORDER_ID, ORDER_NUMBER, CancelReason.CHANGE_OF_MIND, "단순 변심", null, List.of(itemRequest)
            );

            when(cancelRepository.existsByOrderIdAndUserIdAndStatusIn(anyLong(), anyLong(), any()))
                    .thenReturn(false);
            when(orderServiceClient.getMyOrder(ORDER_ID))
                    .thenReturn(ApiResponse.success(userOrderPayload("PENDING")));
            when(cancelRepository.save(any(Cancel.class))).thenAnswer(invocation -> {
                Cancel cancel = invocation.getArgument(0);
                ReflectionTestUtils.setField(cancel, "id", CANCEL_ID);
                return cancel;
            });
            doNothing().when(outboxEventPublisher).publishCancelRequestedEvent(any());

            // when
            CancelResponse response = cancelService.createCancel(USER_ID, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getOrderId()).isEqualTo(ORDER_ID);
            assertThat(response.getUserId()).isEqualTo(USER_ID);
            assertThat(response.getCancelReason()).isEqualTo(CancelReason.CHANGE_OF_MIND);
            assertThat(response.getStatus()).isEqualTo(CancelStatus.REQUESTED);
            assertThat(response.getRequestType()).isEqualTo(CancelRequestType.ORDER_CANCEL);
            verify(cancelRepository).save(any(Cancel.class));
            verify(outboxEventPublisher).publishCancelRequestedEvent(any());
        }

        @Test
        @DisplayName("쿠폰·할인이 있는 주문은 취소 품목 단가에 할인 비율이 반영된다")
        void createCancel_appliesOrderDiscountToUnitPrice() {
            CancelItemRequest itemRequest = new CancelItemRequest(
                    1L, "테스트 상품", 2, new BigDecimal("10000")
            );
            CreateCancelRequest request = new CreateCancelRequest(
                    ORDER_ID, ORDER_NUMBER, CancelReason.CHANGE_OF_MIND, "단순 변심", null, List.of(itemRequest)
            );

            when(cancelRepository.existsByOrderIdAndUserIdAndStatusIn(anyLong(), anyLong(), any()))
                    .thenReturn(false);
            when(orderServiceClient.getMyOrder(ORDER_ID))
                    .thenReturn(ApiResponse.success(discountOrderPayload("PENDING")));
            when(cancelRepository.save(any(Cancel.class))).thenAnswer(invocation -> {
                Cancel cancel = invocation.getArgument(0);
                ReflectionTestUtils.setField(cancel, "id", CANCEL_ID);
                return cancel;
            });
            doNothing().when(outboxEventPublisher).publishCancelRequestedEvent(any());

            CancelResponse response = cancelService.createCancel(USER_ID, request);

            assertThat(response.getItems()).hasSize(1);
            assertThat(response.getItems().get(0).getUnitPrice()).isEqualByComparingTo(new BigDecimal("500.00"));
            assertThat(response.getItems().get(0).getTotalPrice()).isEqualByComparingTo(new BigDecimal("1000.00"));
        }

        @Test
        @DisplayName("반품·환불 요청 생성 시 requestType 저장")
        void createCancel_returnRefundType_success() {
            CancelItemRequest itemRequest = new CancelItemRequest(
                    1L, "테스트 상품", 2, new BigDecimal("10000")
            );
            CreateCancelRequest request = new CreateCancelRequest(
                    ORDER_ID, ORDER_NUMBER, CancelReason.CHANGE_OF_MIND, "단순 변심",
                    CancelRequestType.RETURN_REFUND, List.of(itemRequest)
            );

            when(cancelRepository.existsByOrderIdAndUserIdAndStatusIn(anyLong(), anyLong(), any()))
                    .thenReturn(false);
            when(orderServiceClient.getMyOrder(ORDER_ID))
                    .thenReturn(ApiResponse.success(userOrderPayload("DELIVERED")));
            when(cancelRepository.save(any(Cancel.class))).thenAnswer(invocation -> {
                Cancel cancel = invocation.getArgument(0);
                ReflectionTestUtils.setField(cancel, "id", CANCEL_ID);
                return cancel;
            });
            doNothing().when(outboxEventPublisher).publishCancelRequestedEvent(any());

            CancelResponse response = cancelService.createCancel(USER_ID, request);

            assertThat(response.getRequestType()).isEqualTo(CancelRequestType.RETURN_REFUND);
        }

        @Test
        @DisplayName("반품·환불: DB status는 PENDING이어도 progressStatus 배송완료면 허용(스킵 결제·집계 선행)")
        void createCancel_returnRefund_allowedWhenProgressDeliveredOnly() {
            CancelItemRequest itemRequest = new CancelItemRequest(
                    1L, "테스트 상품", 2, new BigDecimal("10000")
            );
            CreateCancelRequest request = new CreateCancelRequest(
                    ORDER_ID, ORDER_NUMBER, CancelReason.CHANGE_OF_MIND, "단순 변심",
                    CancelRequestType.RETURN_REFUND, List.of(itemRequest)
            );

            OrderPayload payload = userOrderPayload("PENDING");
            payload.setProgressStatus("DELIVERED");

            when(cancelRepository.existsByOrderIdAndUserIdAndStatusIn(anyLong(), anyLong(), any()))
                    .thenReturn(false);
            when(orderServiceClient.getMyOrder(ORDER_ID)).thenReturn(ApiResponse.success(payload));
            when(cancelRepository.save(any(Cancel.class))).thenAnswer(invocation -> {
                Cancel cancel = invocation.getArgument(0);
                ReflectionTestUtils.setField(cancel, "id", CANCEL_ID);
                return cancel;
            });
            doNothing().when(outboxEventPublisher).publishCancelRequestedEvent(any());

            CancelResponse response = cancelService.createCancel(USER_ID, request);

            assertThat(response.getRequestType()).isEqualTo(CancelRequestType.RETURN_REFUND);
            verify(outboxEventPublisher).publishCancelRequestedEvent(any());
        }

        @Test
        @DisplayName("취소 요청 생성 실패 - 배송 중")
        void createCancel_shippingBlocked() {
            CancelItemRequest itemRequest = new CancelItemRequest(
                    1L, "테스트 상품", 2, new BigDecimal("10000")
            );
            CreateCancelRequest request = new CreateCancelRequest(
                    ORDER_ID, ORDER_NUMBER, CancelReason.CHANGE_OF_MIND, "단순 변심", null, List.of(itemRequest)
            );

            when(cancelRepository.existsByOrderIdAndUserIdAndStatusIn(anyLong(), anyLong(), any()))
                    .thenReturn(false);
            when(orderServiceClient.getMyOrder(ORDER_ID))
                    .thenReturn(ApiResponse.success(userOrderPayload("SHIPPING")));

            assertThatThrownBy(() -> cancelService.createCancel(USER_ID, request))
                    .isInstanceOf(CancelDomainException.class)
                    .hasMessageContaining("배송 중");
        }

        @Test
        @DisplayName("취소 요청 생성 실패 - 빈 상품 목록")
        void createCancel_emptyItems_throwsException() {
            // given
            CreateCancelRequest request = new CreateCancelRequest(
                    ORDER_ID, ORDER_NUMBER, CancelReason.CHANGE_OF_MIND, "단순 변심", null, List.of()
            );

            // when & then
            assertThatThrownBy(() -> cancelService.createCancel(USER_ID, request))
                    .isInstanceOf(CancelDomainException.class)
                    .hasMessageContaining("취소 상품이 비어있습니다");
        }

        @Test
        @DisplayName("취소 요청 생성 실패 - null 상품 목록")
        void createCancel_nullItems_throwsException() {
            // given
            CreateCancelRequest request = new CreateCancelRequest(
                    ORDER_ID, ORDER_NUMBER, CancelReason.CHANGE_OF_MIND, "단순 변심", null, null
            );

            // when & then
            assertThatThrownBy(() -> cancelService.createCancel(USER_ID, request))
                    .isInstanceOf(CancelDomainException.class)
                    .hasMessageContaining("취소 상품이 비어있습니다");
        }

        @Test
        @DisplayName("취소 요청 생성 실패 - 동일 주문에 진행 중인 취소가 이미 있음")
        void createCancel_duplicate_throwsException() {
            CancelItemRequest itemRequest = new CancelItemRequest(
                    1L, "테스트 상품", 2, new BigDecimal("10000")
            );
            CreateCancelRequest request = new CreateCancelRequest(
                    ORDER_ID, ORDER_NUMBER, CancelReason.CHANGE_OF_MIND, "단순 변심", null, List.of(itemRequest)
            );

            when(cancelRepository.existsByOrderIdAndUserIdAndStatusIn(anyLong(), anyLong(), any()))
                    .thenReturn(true);

            assertThatThrownBy(() -> cancelService.createCancel(USER_ID, request))
                    .isInstanceOf(CancelDomainException.class)
                    .hasMessageContaining("이미 접수");
        }

        @Test
        @DisplayName("취소 요청 생성 실패 - 동일 유형이 이미 거절됨")
        void createCancel_sameTypeRejected_throwsException() {
            CancelItemRequest itemRequest = new CancelItemRequest(
                    1L, "테스트 상품", 2, new BigDecimal("10000")
            );
            CreateCancelRequest request = new CreateCancelRequest(
                    ORDER_ID, ORDER_NUMBER, CancelReason.CHANGE_OF_MIND, "단순 변심", null, List.of(itemRequest)
            );

            when(cancelRepository.existsByOrderIdAndUserIdAndStatusIn(anyLong(), anyLong(), any()))
                    .thenReturn(false);
            when(cancelRepository.existsByOrderIdAndUserIdAndStatusAndRequestType(
                    eq(ORDER_ID), eq(USER_ID), eq(CancelStatus.REJECTED), eq(CancelRequestType.ORDER_CANCEL)))
                    .thenReturn(true);

            assertThatThrownBy(() -> cancelService.createCancel(USER_ID, request))
                    .isInstanceOf(CancelDomainException.class)
                    .hasMessageContaining("거절");
        }

        @Test
        @DisplayName("반품·환불 요청 시 거절 이력은 RETURN_REFUND 유형만 조회(주문 취소 거절은 재검사하지 않음)")
        void createCancel_returnRefund_rejectionCheckUsesRequestTypeOnly() {
            CancelItemRequest itemRequest = new CancelItemRequest(
                    1L, "테스트 상품", 2, new BigDecimal("10000")
            );
            CreateCancelRequest request = new CreateCancelRequest(
                    ORDER_ID, ORDER_NUMBER, CancelReason.CHANGE_OF_MIND, "단순 변심",
                    CancelRequestType.RETURN_REFUND, List.of(itemRequest)
            );

            when(cancelRepository.existsByOrderIdAndUserIdAndStatusIn(anyLong(), anyLong(), any()))
                    .thenReturn(false);
            when(cancelRepository.existsByOrderIdAndUserIdAndStatusAndRequestType(
                    eq(ORDER_ID), eq(USER_ID), eq(CancelStatus.REJECTED), eq(CancelRequestType.RETURN_REFUND)))
                    .thenReturn(false);
            when(orderServiceClient.getMyOrder(ORDER_ID))
                    .thenReturn(ApiResponse.success(userOrderPayload("DELIVERED")));
            when(cancelRepository.save(any(Cancel.class))).thenAnswer(invocation -> {
                Cancel cancel = invocation.getArgument(0);
                ReflectionTestUtils.setField(cancel, "id", CANCEL_ID);
                return cancel;
            });
            doNothing().when(outboxEventPublisher).publishCancelRequestedEvent(any());

            CancelResponse response = cancelService.createCancel(USER_ID, request);

            assertThat(response.getRequestType()).isEqualTo(CancelRequestType.RETURN_REFUND);
            verify(cancelRepository).existsByOrderIdAndUserIdAndStatusAndRequestType(
                    ORDER_ID, USER_ID, CancelStatus.REJECTED, CancelRequestType.RETURN_REFUND);
            verify(cancelRepository, never()).existsByOrderIdAndUserIdAndStatusAndRequestType(
                    eq(ORDER_ID), eq(USER_ID), eq(CancelStatus.REJECTED), eq(CancelRequestType.ORDER_CANCEL));
            verify(outboxEventPublisher).publishCancelRequestedEvent(any());
        }
    }

    @Nested
    @DisplayName("취소 조회")
    class GetCancelTest {

        @Test
        @DisplayName("취소 단건 조회 성공 (사용자)")
        void getCancel_success() {
            // given
            when(cancelRepository.findByIdAndUserIdWithItems(CANCEL_ID, USER_ID))
                    .thenReturn(Optional.of(testCancel));
            when(orderServiceClient.getMyOrder(ORDER_ID))
                    .thenReturn(ApiResponse.success(discountOrderPayload("PENDING")));

            // when
            CancelResponse response = cancelService.getCancel(CANCEL_ID, USER_ID);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(CANCEL_ID);
            assertThat(response.getUserId()).isEqualTo(USER_ID);
            assertThat(response.getItems().get(0).getUnitPrice()).isEqualByComparingTo(new BigDecimal("500.00"));
        }

        @Test
        @DisplayName("취소 조회 실패 - 존재하지 않음")
        void getCancel_notFound_throwsException() {
            // given
            when(cancelRepository.findByIdAndUserIdWithItems(999L, USER_ID))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cancelService.getCancel(999L, USER_ID))
                    .isInstanceOf(CancelDomainException.class)
                    .hasMessageContaining("취소 요청을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("주문별 진행 중 취소 요약 조회 성공")
        void getActiveCancelForOrder_found() {
            when(cancelRepository.findFirstByOrderIdAndUserIdAndStatusInOrderByIdDesc(eq(ORDER_ID), eq(USER_ID), any()))
                    .thenReturn(Optional.of(testCancel));

            var opt = cancelService.getActiveCancelForOrder(ORDER_ID, USER_ID);

            assertThat(opt).isPresent();
            assertThat(opt.get().getStatus()).isEqualTo(CancelStatus.REQUESTED);
            assertThat(opt.get().getRequestType()).isEqualTo(CancelRequestType.ORDER_CANCEL);
            assertThat(opt.get().getCancelNumber()).isEqualTo(testCancel.getCancelNumber());
        }

        @Test
        @DisplayName("주문별 진행 중 취소 요약 없음")
        void getActiveCancelForOrder_empty() {
            when(cancelRepository.findFirstByOrderIdAndUserIdAndStatusInOrderByIdDesc(eq(ORDER_ID), eq(USER_ID), any()))
                    .thenReturn(Optional.empty());

            assertThat(cancelService.getActiveCancelForOrder(ORDER_ID, USER_ID)).isEmpty();
        }

        @Test
        @DisplayName("내 취소 목록 조회")
        void getMyCancels_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Cancel> cancelPage = new PageImpl<>(List.of(testCancel), pageable, 1);

            when(cancelRepository.findByUserId(USER_ID, pageable)).thenReturn(cancelPage);

            // when
            PageResponse<CancelResponse> response = cancelService.getMyCancels(USER_ID, pageable);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("전체 취소 목록 조회")
        void getAllCancels_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Cancel> cancelPage = new PageImpl<>(List.of(testCancel), pageable, 1);

            when(cancelRepository.findAll(pageable)).thenReturn(cancelPage);

            // when
            PageResponse<CancelResponse> response = cancelService.getAllCancels(pageable);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("상태별 취소 목록 조회")
        void getCancelsByStatus_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Cancel> cancelPage = new PageImpl<>(List.of(testCancel), pageable, 1);

            when(cancelRepository.findByStatus(CancelStatus.REQUESTED, pageable)).thenReturn(cancelPage);

            // when
            PageResponse<CancelResponse> response = cancelService.getCancelsByStatus(CancelStatus.REQUESTED, pageable);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getStatus()).isEqualTo(CancelStatus.REQUESTED);
        }

        @Test
        @DisplayName("관리자 취소 단건 조회 성공")
        void getCancelById_success() {
            // given
            when(cancelRepository.findByIdWithItems(CANCEL_ID)).thenReturn(Optional.of(testCancel));
            when(orderServiceClient.getAdminOrder(ORDER_ID))
                    .thenReturn(ApiResponse.success(adminOrderPayloadForRefund("REQUESTED", "PREPARING")));

            // when
            CancelResponse response = cancelService.getCancelById(CANCEL_ID);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(CANCEL_ID);
            assertThat(response.getItems()).hasSize(1);
            assertThat(response.getItems().get(0).getUnitPrice()).isEqualByComparingTo(new BigDecimal("500.00"));
        }

        @Test
        @DisplayName("관리자 취소 조회 실패 - 존재하지 않음")
        void getCancelById_notFound_throwsException() {
            // given
            when(cancelRepository.findByIdWithItems(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cancelService.getCancelById(999L))
                    .isInstanceOf(CancelDomainException.class)
                    .hasMessageContaining("취소 요청을 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("취소 승인")
    class ApproveCancelTest {

        @Test
        @DisplayName("취소 승인 성공")
        void approveCancel_success() {
            // given
            when(cancelRepository.findByIdWithItems(CANCEL_ID)).thenReturn(Optional.of(testCancel));
            when(orderServiceClient.getAdminOrder(ORDER_ID)).thenReturn(
                    ApiResponse.success(adminOrderPayloadForRefund("CANCEL_REQUESTED", "CONFIRMED")));
            doNothing().when(outboxEventPublisher).publishCancelApprovedEvent(any());

            // when
            CancelResponse response = cancelService.approveCancel(CANCEL_ID);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(CancelStatus.APPROVED);
            ArgumentCaptor<CancelApprovedEvent> captor = ArgumentCaptor.forClass(CancelApprovedEvent.class);
            verify(outboxEventPublisher).publishCancelApprovedEvent(captor.capture());
            assertThat(captor.getValue().getRefundAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
            assertThat(captor.getValue().getRequestType()).isEqualTo(CancelRequestType.ORDER_CANCEL);
        }

        @Test
        @DisplayName("반품·환불 승인 시 CancelApprovedEvent에 RETURN_REFUND requestType 포함")
        void approveCancel_returnRefund_includesRequestTypeOnEvent() {
            Cancel returnCancel = Cancel.create(
                    ORDER_ID, ORDER_NUMBER, USER_ID, CancelReason.CHANGE_OF_MIND, "단순 변심",
                    CancelRequestType.RETURN_REFUND);
            ReflectionTestUtils.setField(returnCancel, "id", CANCEL_ID);
            ReflectionTestUtils.setField(returnCancel, "cancelNumber", CANCEL_NUMBER);
            ReflectionTestUtils.setField(returnCancel, "status", CancelStatus.REQUESTED);
            CancelItem item = CancelItem.create(1L, "테스트 상품", 2, new BigDecimal("10000"));
            ReflectionTestUtils.setField(item, "id", 1L);
            returnCancel.addCancelItem(item);

            when(cancelRepository.findByIdWithItems(CANCEL_ID)).thenReturn(Optional.of(returnCancel));
            when(orderServiceClient.getAdminOrder(ORDER_ID)).thenReturn(
                    ApiResponse.success(adminOrderPayloadForRefund("CANCEL_REQUESTED", "DELIVERED")));
            doNothing().when(outboxEventPublisher).publishCancelApprovedEvent(any());

            cancelService.approveCancel(CANCEL_ID);

            ArgumentCaptor<CancelApprovedEvent> captor = ArgumentCaptor.forClass(CancelApprovedEvent.class);
            verify(outboxEventPublisher).publishCancelApprovedEvent(captor.capture());
            assertThat(captor.getValue().getRequestType()).isEqualTo(CancelRequestType.RETURN_REFUND);
        }

        @Test
        @DisplayName("취소 승인 실패 - 존재하지 않음")
        void approveCancel_notFound_throwsException() {
            // given
            when(cancelRepository.findByIdWithItems(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cancelService.approveCancel(999L))
                    .isInstanceOf(CancelDomainException.class)
                    .hasMessageContaining("취소 요청을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("취소 승인 실패 - 요청 상태가 아님")
        void approveCancel_notInRequestedStatus_throwsException() {
            // given
            Cancel approvedCancel = createTestCancel(2L, ORDER_ID, USER_ID, ORDER_NUMBER, CancelStatus.APPROVED);

            when(cancelRepository.findByIdWithItems(2L)).thenReturn(Optional.of(approvedCancel));

            // when & then
            assertThatThrownBy(() -> cancelService.approveCancel(2L))
                    .isInstanceOf(CancelDomainException.class)
                    .hasMessageContaining("취소 요청 상태가 아닙니다");
        }
    }

    @Nested
    @DisplayName("취소 거부")
    class RejectCancelTest {

        @Test
        @DisplayName("취소 거부 성공")
        void rejectCancel_success() {
            // given
            String rejectedReason = "재고 부족으로 인한 거부";

            when(cancelRepository.findByIdWithItems(CANCEL_ID)).thenReturn(Optional.of(testCancel));
            when(orderServiceClient.getAdminOrder(ORDER_ID)).thenReturn(
                    ApiResponse.success(adminOrderPayload("CANCEL_REQUESTED", "PREPARING")));
            doNothing().when(outboxEventPublisher).publishCancelRejectedEvent(any());

            // when
            CancelResponse response = cancelService.rejectCancel(CANCEL_ID, rejectedReason);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(CancelStatus.REJECTED);
            assertThat(response.getRejectedReason()).isEqualTo(rejectedReason);
            verify(outboxEventPublisher).publishCancelRejectedEvent(any());
        }

        @Test
        @DisplayName("취소 거부 실패 - 존재하지 않음")
        void rejectCancel_notFound_throwsException() {
            // given
            when(cancelRepository.findByIdWithItems(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cancelService.rejectCancel(999L, "사유"))
                    .isInstanceOf(CancelDomainException.class)
                    .hasMessageContaining("취소 요청을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("취소 거부 실패 - 요청 상태가 아님")
        void rejectCancel_notInRequestedStatus_throwsException() {
            // given
            Cancel rejectedCancel = createTestCancel(2L, ORDER_ID, USER_ID, ORDER_NUMBER, CancelStatus.REJECTED);

            when(cancelRepository.findByIdWithItems(2L)).thenReturn(Optional.of(rejectedCancel));

            // when & then
            assertThatThrownBy(() -> cancelService.rejectCancel(2L, "사유"))
                    .isInstanceOf(CancelDomainException.class)
                    .hasMessageContaining("취소 요청 상태가 아닙니다");
        }
    }

    private static OrderPayload userOrderPayload(String status) {
        OrderPayload p = new OrderPayload();
        p.setStatus(status);
        return p;
    }

    /** total 20000 ??final 1000, ??   1????   ?????    ????? ?    */
    private static OrderPayload discountOrderPayload(String status) {
        OrderPayload p = userOrderPayload(status);
        p.setTotalAmount(new BigDecimal("20000"));
        p.setFinalAmount(new BigDecimal("1000"));
        OrderPayload.OrderItemLinePayload line = new OrderPayload.OrderItemLinePayload();
        line.setProductId(1L);
        line.setQuantity(2);
        line.setTotalPrice(new BigDecimal("20000"));
        p.setItems(List.of(line));
        return p;
    }

    private static OrderPayload adminOrderPayload(String status, String statusBefore) {
        OrderPayload p = new OrderPayload();
        p.setStatus(status);
        p.setStatusBeforeCancelRequest(statusBefore);
        return p;
    }

    /**
     * testCancel ??  : productId=1, quantity=2, ??   ??20000????finalAmount 1000? ?  ??       ????  ??1000??
     */
    private static OrderPayload adminOrderPayloadForRefund(String status, String statusBefore) {
        OrderPayload p = adminOrderPayload(status, statusBefore);
        p.setTotalAmount(new BigDecimal("20000"));
        p.setFinalAmount(new BigDecimal("1000"));
        OrderPayload.OrderItemLinePayload line = new OrderPayload.OrderItemLinePayload();
        line.setProductId(1L);
        line.setQuantity(2);
        line.setTotalPrice(new BigDecimal("20000"));
        p.setItems(List.of(line));
        return p;
    }

    private Cancel createTestCancel(Long id, Long orderId, Long userId, String orderNumber, CancelStatus status) {
        Cancel cancel = Cancel.create(
                orderId, orderNumber, userId, CancelReason.CHANGE_OF_MIND, "단순 변심",
                CancelRequestType.ORDER_CANCEL
        );
        ReflectionTestUtils.setField(cancel, "id", id);
        ReflectionTestUtils.setField(cancel, "cancelNumber", CANCEL_NUMBER);
        ReflectionTestUtils.setField(cancel, "status", status);

        CancelItem item = CancelItem.create(1L, "테스트 상품", 2, new BigDecimal("10000"));
        ReflectionTestUtils.setField(item, "id", 1L);
        cancel.addCancelItem(item);

        return cancel;
    }
}
