# Sparta e-commerce MSA 아키텍처 (Sparta E-commerce MSA Architecture)

본 프로젝트는 e-커머스 플랫폼을 위한 마이크로서비스 아키텍처(MSA) 기반의 통합 시스템입니다. 도메인 영역별로 분리된 백엔드 서비스들과 React 기반의 프론트엔드로 구성되어 있습니다.

## 1. 기술 스택 (Tech Stack)
- **Backend**: Spring Boot, Spring Cloud Gateway, Spring Data JPA
- **Frontend**: React, TypeScript, Vite
- **Database**: MySQL 8.0 (Database per Service 패턴 적용)
- **Cache & Session**: Redis
- **Message Broker**: Kafka, Zookeeper (비동기 이벤트 처리)
- **Infrastructure**: Docker, Docker Compose

---

## 2. 마이크로서비스 구성 (Microservices Architecture)
API Gateway를 통해 클라이언트 요청을 라우팅하며, 총 7개의 내부 마이크로서비스로 비즈니스 로직을 분리하였습니다.

| 서비스명             | 포트   | 주요 역할 및 기능                                               | 개별 데이터베이스            |
| -------------------- | ------ | --------------------------------------------------------------- | ---------------------------- |
| **Gateway Service**  | `8000` | 클라이언트 요청 통합 라우팅, JWT 인증 검증 (Auth/Global Filter) | -                            |
| **User Service**     | `8080` | 회원가입/로그인 (JWT 발급), 회원 프로필/권한(USER, ADMIN) 관리  | `user-mysql` (port 3307)     |
| **Product Service**  | `8081` | 상품/카테고리 CRUD, 재고 관리(차감/복구), 상품 조회, 찜 기능    | `product-mysql` (port 3308)  |
| **Order Service**    | `8082` | 장바구니/주문 생성, 주문 상태 관리, Aggregation 처리            | `order-mysql` (port 3309)    |
| **Discount Service** | `8083` | 옵션 할인/쿠폰 관리 및 할인 검증                                | `discount-mysql` (port 3310) |
| **Payment Service**  | `8084` | 외부 결제 API 연동 정보 보관, 결제 내역 생성 및 취소 선행 작업  | `payment-mysql` (port 3311)  |
| **Cancel Service**   | `8085` | 주문 취소 시 로직 처리 내역 관리                                | `cancel-mysql` (port 3312)   |
| **Refund Service**   | `8086` | 환불 상태 관리 및 환불 금액(전체/부분) 정산 처리 완료           | `refund-mysql` (port 3313)   |

---

## 3. 프론트엔드 (Frontend)
- **Client (React 응용프로그램)**: 포트 `3000`으로 매핑되어 서비스 제공
- 클라이언트는 개별 백엔드 서비스에 직접 접근하지 않고, 반드시 **Gateway Service (8000번 포트)** 를 통해서만 통신 백엔드 요청을 수행합니다.

---

## 4. 인프라 및 미들웨어 (Infrastructure & Middleware)
서비스 확장을 고려하여 인프라는 `docker-compose`를 사용해 컨테이너화되어 중앙 관리됩니다.

### 🍅 개별 데이터베이스 분리 설계 (Database per Service)
각 마이크로서비스가 본인만의 데이터베이스 인스턴스를 유지하여 느슨한 결합(Loose Coupling)을 보장합니다.

### 🍅 Redis (`6379`)
- 전역 설정되어 모든 서비스들이 함께 공유합니다.
- **인증 보안**: 로그아웃 등에서 발생하는 JWT 블랙리스트 캐싱
- **성능 최적화**: 잦은 읽기 및 변경이 발생하는 장바구니, 찜 목록 캐시 관리 전략 

### 🍅 Kafka & Zookeeper (`9092`, `2181`)
- **비동기 이벤트 프로세싱**: 트래픽이 심하거나 오랫동안 지연될 수 있는 특정 프로세스를 백그라운드로 메시지를 활용하여 전송 및 처리(Publish & Consume).
- **분산 트랜잭션 관리 (Saga Pattern)**: 여러 개의 서비스에 걸쳐 발생하는 트랜잭션 중 중간에 에러가 났을 경우 데이터 정합성을 위한 보상 트랜잭션(롤백) 이벤트를 발생시켜 각 서비스에 분산 처리합니다. (예: 결제 도중 실패 시, 이전에 차감해 둔 상품 도메인에서의 재고를 원복시키는 카프카 이벤트 전송)

---

## 5. 전체 트랜잭션 흐름 통신 예시

1. **사용자 요청 진입**: Frontend를 통해 사용자가 결제 요청을 수행합니다.
2. **Gateway 라우팅**: 요청은 Gateway(포트 8000)로 들어가 JWT에 대한 검증을 완료한 후, 알맞은 Order 또는 Payment 서비스로 포워딩 됩니다.
3. **재고 차감 (동기/비동기)**: Product 서비스에 재고 차감을 동기 API 호출(혹은 비동기 이벤트)을 통해 요청합니다. 
4. **결제 진행 및 발행**: 결제 처리가 외부 연동을 통해 완료되면 처리 성공 이벤트를 Kafka로 발행합니다.
5. **롤백 (보상 트랜잭션) 수행**: 도중 Product 서비스에서 재고가 부족해 에러가 발생하거나 Payment 외부 통신 에러가 날 경우, `cancel-service` 또는 `refund-service` 쪽에 롤백을 전파하고, Kafka 보상 이벤트를 발행하여 Order 쪽 트랜잭션을 취소 상태로 바꿉니다.
