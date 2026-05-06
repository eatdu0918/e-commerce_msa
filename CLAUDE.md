# CLAUDE.md - Sparta MSA E-Commerce Project

## 프로젝트 개요

Spring Boot 3.2.11 기반 MSA e-commerce 플랫폼. Java 17, Gradle 멀티 모듈.

- **Group**: `com.ecommerce`
- **Backend**: Spring Boot 3.2.11, Spring Cloud Gateway, Spring Security, Spring Data JPA, Spring Kafka
- **Frontend**: React 19 + TypeScript + Vite 7 + TailwindCSS 4 + TanStack Query 5 + Axios
- **Infra**: MySQL 8.0 (서비스별 독립 DB), Redis (JWT 블랙리스트/리프레시 토큰), Kafka (Confluent 7.5.0)
- **JWT**: JJWT 0.12.5, Access Token 30분, Refresh Token 7일
- **DB 마이그레이션**: Flyway (`ddl-auto: none`)

## 서비스 맵

| 서비스 | 포트 | DB (MySQL 포트) | 패키지 | API 경로 |
|--------|------|-----------------|--------|----------|
| gateway-service | 8000 | - | `com.ecommerce.gatewayservice` | 라우팅 전용 |
| user-service | 8080 | user_db (3307) | `com.ecommerce.userservice` | `/api/auth/**`, `/api/users/**` |
| product-service | 8081 | product_db (3308) | `com.ecommerce.productservice` | `/api/products/**` |
| order-service | 8082 | order_db (3309) | `com.ecommerce.orderservice` | `/api/orders/**` |
| discount-service | 8083 | discount_db (3310) | `com.ecommerce.discountservice` | `/api/discounts/**` |
| payment-service | 8084 | payment_db (3311) | `com.ecommerce.paymentservice` | `/api/payments/**` |
| cancel-service | 8085 | cancel_db (3312) | `com.ecommerce.cancelservice` | `/api/cancels/**` |
| refund-service | 8086 | refund_db (3313) | `com.ecommerce.refundservice` | `/api/refunds/**` |
| client (React) | 3000/5173 | - | - | Vite dev server |

## 패키지 구조 (각 서비스 공통)

```
com.ecommerce.{servicename}/
├── config/          # SecurityConfig, RedisConfig, KafkaConfig, SwaggerConfig
├── controller/      # REST 컨트롤러
├── dto/
│   ├── request/     # 요청 DTO (@Valid 검증)
│   └── response/    # 응답 DTO (static from() 팩토리)
├── entity/          # JPA 엔티티, BaseEntity
├── enums/           # 상태/역할 열거형
├── event/           # Kafka 이벤트 객체
├── exception/       # DomainException, ExceptionCode enum, GlobalExceptionHandler
├── kafka/           # EventProducer, EventConsumer
├── repository/      # Spring Data JPA Repository
├── response/        # ApiResponse<T> 공통 응답 래퍼
├── security/        # JWT 필터, CustomUserDetails
│   └── jwt/         # JwtTokenProvider, JwtAuthenticationFilter, JwtProperties
└── service/         # 비즈니스 로직
```

## 엔티티 패턴

```java
@Entity
@Table(name = "테이블명", indexes = { @Index(...) })
@EntityListeners(AuditingEntityListener.class)
@Getter
@DynamicInsert
@DynamicUpdate
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class DomainEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "entity_id")
    Long id;

    // @Builder.Default로 기본값 지정
    @Builder.Default
    Boolean isActive = true;

    // 정적 팩토리 메서드 - 생성 진입점
    public static DomainEntity create(...) {
        return DomainEntity.builder()...build();
    }

    // 비즈니스 메서드 (상태 전이)
    public void approve() { ... }
    public void cancel() { ... }
}
```

**BaseEntity**: `@MappedSuperclass`, `createdAt`/`updatedAt` 자동 관리 (`@PrePersist`/`@PreUpdate`)

**핵심 규칙**:
- `@DynamicInsert` + `@DynamicUpdate` 필수
- `@FieldDefaults(level = AccessLevel.PRIVATE)` 필수
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` (JPA 프록시용)
- `@AllArgsConstructor(access = AccessLevel.PRIVATE)` (Builder 전용)
- 생성은 반드시 `static create()` 팩토리 메서드 사용
- 소프트 삭제: `isActive` + `deletedAt` 패턴
- 금액: `BigDecimal`, `@Column(precision = 12, scale = 2)`

## Controller 패턴

```java
@RestController
@RequestMapping("/api/도메인")
@RequiredArgsConstructor
@Slf4j
public class DomainController {

    private final DomainService domainService;

    // 조회 - 페이징
    @GetMapping
    public ApiResponse<PageResponse<DomainResponse>> list(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) { ... }

    // 조회 - 단건
    @GetMapping("/{id}")
    public ApiResponse<DomainResponse> get(@PathVariable Long id) { ... }

    // 생성 (인증 필요)
    @PostMapping
    public ApiResponse<DomainResponse> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateDomainRequest request) { ... }

    // 수정 (인증 필요)
    @PutMapping("/{id}")
    public ApiResponse<DomainResponse> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateDomainRequest request) { ... }
}
```

**핵심 규칙**:
- 응답은 항상 `ApiResponse<T>` 또는 `ResponseEntity<ApiResponse<T>>`로 래핑
- 인증된 사용자 정보: `@AuthenticationPrincipal CustomUserDetails`
- 페이징: `@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)`
- 로그: 각 엔드포인트 진입 시 `log.info()` 기록 (한국어)

## Service 패턴

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class DomainService {

    // 조회 메서드 - readOnly
    @Transactional(readOnly = true)
    public DomainResponse getDomain(Long id) { ... }

    // 변경 메서드
    @Transactional
    public DomainResponse createDomain(CreateDomainRequest request) { ... }
}
```

**핵심 규칙**:
- 조회: `@Transactional(readOnly = true)`
- 생성/수정/삭제: `@Transactional`
- 예외: `throw new XxxDomainException(XxxDomainExceptionCode.ErrorName)`
- 응답 변환: `DomainResponse.from(entity)` 정적 메서드 또는 서비스 내 private 변환 메서드

## DTO 패턴

### Request DTO
```java
@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateDomainRequest {
    @NotBlank(message = "이름은 필수입니다.")        // 한국어 검증 메시지
    String name;

    @NotNull(message = "가격은 필수입니다.")
    @DecimalMin(value = "0", message = "가격은 0 이상이어야 합니다.")
    BigDecimal price;
}
```

### Response DTO
```java
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DomainResponse {
    Long id;
    String name;
    LocalDateTime createdAt;

    // 정적 팩토리 메서드
    public static DomainResponse from(DomainEntity entity) {
        return DomainResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
```

### PageResponse (공통 페이징 DTO)
```java
public static <T> PageResponse<T> from(Page<T> page) { ... }
```

**검증 어노테이션**: `@NotBlank`(문자열), `@NotNull`(객체), `@NotEmpty`(컬렉션), `@Email`, `@Pattern`, `@Size`, `@Min`, `@Max`, `@DecimalMin`, `@Positive`, `@Valid`(중첩 DTO)

## 예외 처리 패턴

```java
// 1. 도메인 예외 - 서비스별 {Domain}DomainException
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderDomainException extends RuntimeException {
    HttpStatus httpStatus;
    String code;

    public OrderDomainException(OrderDomainExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.httpStatus = exceptionCode.getStatus();
        this.code = exceptionCode.name();
    }
}

// 2. 예외 코드 enum
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum OrderDomainExceptionCode {
    OrderNotFoundException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    // 한국어 메시지
    ;
    final HttpStatus status;
    final String message;
}

// 3. GlobalExceptionHandler - @RestControllerAdvice
// 핸들링: DomainException, MethodArgumentNotValidException, Exception
```

## ApiResponse 공통 응답

```java
ApiResponse.ok()                    // 성공, 데이터 없음
ApiResponse.success(data)           // 성공 + 데이터
ApiResponse.success(data, "메시지")  // 성공 + 데이터 + 메시지
ApiResponse.error("에러 메시지")     // 실패
```

구조: `{ success, message, error: { errorCode, errorMessage }, data, timestamp }`
- `@JsonInclude(JsonInclude.Include.NON_NULL)` - null 필드 제외

## Kafka 이벤트 패턴

### 토픽 목록

| 서비스 | 토픽 | 방향 |
|--------|------|------|
| order-service | `order-created` | Produce |
| order-service | `order-cancelled` | Produce |
| product-service | `stock-decreased` | Produce |
| product-service | `stock-decrease-failed` | Produce |
| discount-service | `coupon-used` | Produce |
| discount-service | `coupon-use-failed` | Produce |
| discount-service | `coupon-restored` | Produce |
| payment-service | `payment-completed` | Produce |
| payment-service | `payment-failed` | Produce |
| cancel-service | `cancel-requested` | Produce |
| cancel-service | `cancel-approved` | Produce |
| cancel-service | `cancel-rejected` | Produce |
| refund-service | `refund-completed` | Produce |
| refund-service | `refund-failed` | Produce |

### Saga 주문 플로우

```
주문 생성 → order-created
  → product-service: 재고 차감 → stock-decreased
    → discount-service: 쿠폰 적용 → coupon-used
      → order-service: 주문 확정 (order.confirm())
      → payment-service: 결제 완료 → payment-completed

실패 시 보상 트랜잭션:
  stock-decrease-failed → 주문 취소
  coupon-use-failed → 주문 취소 + order-cancelled → 재고 복구
  order-cancelled → 재고 복구 + 쿠폰 복원 + 결제 취소
```

### Producer 패턴
```java
@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendEvent(DomainEvent event) {
        kafkaTemplate.send(KafkaConfig.TOPIC_NAME, event.getKey(), event);
    }
}
```

### Consumer 패턴
```java
@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventConsumer {

    @KafkaListener(topics = "토픽명", groupId = "서비스명")
    @Transactional
    public void handleEvent(EventType event) {
        // 비즈니스 로직 + 보상 이벤트 발행
    }
}
```

### KafkaConfig 패턴
```java
@Configuration
public class KafkaConfig {
    public static final String TOPIC_XXX = "topic-name";

    @Bean
    public NewTopic topicXxx() {
        return TopicBuilder.name(TOPIC_XXX).partitions(3).replicas(1).build();
    }
}
```

## Security / JWT

- **세션**: `SessionCreationPolicy.STATELESS` (무상태)
- **CSRF**: disabled
- **비밀번호**: BCryptPasswordEncoder
- **JWT 라이브러리**: JJWT 0.12.5
- **필터**: `JwtAuthenticationFilter extends OncePerRequestFilter`
  - `Authorization: Bearer {token}` 헤더에서 추출
  - 토큰 유효성 검증 → Access 토큰 확인 → 블랙리스트 확인 → SecurityContext 설정
- **Redis 토큰 관리**:
  - Refresh Token 저장: `refresh_token:{userId}` (TTL 7일)
  - 블랙리스트: `blacklist:{accessToken}` (TTL = 토큰 잔여 만료 시간)
- **인가**: `@PreAuthorize("isAuthenticated()")`, `hasRole("ADMIN")`
- **CustomUserDetails**: `userId`, `email`, `role` 포함

## Flyway 규칙

- 파일 위치: `src/main/resources/db/migration/`
- 네이밍: `V{n}__{설명}.sql` (언더스코어 2개)
  - 예: `V1__create_user_table.sql`, `V2__add_category_table.sql`
- `ddl-auto: none` (Flyway가 스키마 관리)
- `baseline-on-migrate: true`
- 테이블: InnoDB, `DEFAULT CHARSET=utf8mb4`
- PK: `BIGINT AUTO_INCREMENT`
- 금액: `DECIMAL(12,2)`
- 상태: `VARCHAR(20)`, 기본값 문자열 (`DEFAULT 'PENDING'`)
- 타임스탬프: `DATETIME NOT NULL` (createdAt, updatedAt)

## 네이밍 컨벤션

| 대상 | 규칙 | 예시 |
|------|------|------|
| 클래스 | PascalCase | `OrderService`, `CreateOrderRequest` |
| 메서드/변수 | camelCase | `createOrder()`, `userId` |
| DB 테이블 | snake_case (복수형) | `orders`, `cancel_items` |
| DB 컬럼 | snake_case | `user_id`, `created_at` |
| Kafka 토픽 | kebab-case | `order-created`, `stock-decreased` |
| API 경로 | kebab-case (복수형) | `/api/products`, `/api/orders` |
| 패키지 | 소문자 | `com.ecommerce.orderservice` |
| Enum 값 | UPPER_SNAKE_CASE | `REQUESTED`, `PAYMENT_COMPLETED` |

**한국어 사용 기준**:
- 한국어 사용: 검증 메시지(`message = "이름은 필수입니다."`), 예외 메시지, 로그 메시지, SQL 주석(`COMMENT`)
- 영어 사용: 코드(클래스/메서드/변수명), API 경로, Kafka 토픽, DB 테이블/컬럼명

## 빌드 및 실행 명령어

```bash
# 인프라 (MySQL, Redis, Kafka, Gateway)
docker compose up -d

# 전체 빌드 (Windows)
./gradlew.bat clean build

# 특정 서비스 빌드
./gradlew.bat :order-service:build

# 특정 서비스 실행
./gradlew.bat :user-service:bootRun

# 프론트엔드
cd client && npm install && npm run dev

# Docker 로그 확인
docker logs -f gateway-service
```

## 트러블슈팅

- **MySQL 볼륨 충돌**: `docker compose down -v`로 볼륨 삭제 후 재시작
- **Kafka 지연**: Kafka/Zookeeper 컨테이너가 완전히 시작된 후 서비스 실행 (healthcheck 의존)
- **포트 충돌**: 서비스별 고유 포트 확인 (8080~8086), DB 포트 (3307~3313)
- **Flyway 실패**: 기존 스키마와 충돌 시 `baseline-on-migrate: true` 확인, 또는 DB 볼륨 삭제
- **JWT 시크릿**: 최소 32자 이상 (`your-256-bit-secret-key-here-minimum-32-characters-required`)
- **Kafka 직렬화 오류**: `spring.json.trusted.packages: "*"` 확인
- **CORS 오류**: gateway-service의 `allowedOrigins`에 프론트 주소 포함 확인 (`localhost:3000`, `localhost:5173`)

## 자동 스킬 선택 규칙

**이 규칙은 강제 지시사항이다.** 아래 패턴에 해당하는 요청이 오면, 다른 응답을 생성하기 전에 반드시 해당 Skill을 먼저 호출하라.

### 백엔드 작업
| 요청 패턴 | 자동 호출 Skill |
|-----------|----------------|
| "코드 정리", "리팩토링", "단순화", "중복 제거" | `simplify` |
| "PR 리뷰", "코드 리뷰", "변경사항 리뷰" | `review` |
| "보안 점검", "보안 리뷰", "취약점 확인" | `security-review` |
| "설정 변경", "권한 추가", "hook 설정", "자동화 설정" | `update-config` |
| "권한 프롬프트 줄이기", "허용 목록 추가" | `fewer-permission-prompts` |

### 프론트엔드(React/TypeScript) 작업
| 요청 패턴 | 자동 호출 Skill |
|-----------|----------------|
| "UI 컴포넌트 만들어", "페이지 만들어", "화면 구현" | `frontend-design` |
| "반응형", "모바일 대응", "화면 크기", "breakpoint" | `adapt` |
| "애니메이션", "전환 효과", "모션", "트랜지션" | `animate` |
| "레이아웃 개선", "간격 조정", "정렬", "배치" | `arrange` |
| "색상 추가", "컬러", "단조롭다" | `colorize` |
| "글씨체", "폰트", "타이포그래피", "가독성" | `typeset` |
| "너무 과하다", "과감한 거 줄여", "톤 다운" | `quieter` |
| "심심하다", "밋밋하다", "더 강하게", "임팩트" | `bolder` |
| "마무리", "polish", "출시 전 점검", "완성도" | `polish` |
| "UX 평가", "사용성 평가", "디자인 평가" | `critique` |
| "에러 처리 강화", "엣지 케이스", "견고하게" | `harden` |
| "단순하게", "불필요한 요소 제거", "정리" | `distill` |
| "기술 품질 점검", "접근성 확인", "성능 감사" | `audit` |
| "온보딩", "첫 화면", "빈 상태", "empty state" | `onboard` |
| "즐거움 추가", "개성", "특별한 터치" | `delight` |

### 프로젝트 특화 커맨드 (Skill보다 우선)
| 요청 패턴 | 자동 호출 커맨드 |
|-----------|----------------|
| "새 서비스 만들어", "서비스 추가" | `/새서비스` |
| "API 추가", "엔드포인트 추가" | `/api추가` |
| "Kafka 이벤트", "Saga 구현" | `/saga구현` |
| "마이그레이션 만들어", "SQL 파일 생성" | `/마이그레이션` |
| "서비스 상태", "도커 확인", "헬스체크" | `/상태확인` |
| "코드 리뷰 (MSA 기준)" | `/코드리뷰` |

### 판단 기준
- 요청이 **백엔드 비즈니스 로직**이면 → 프로젝트 커맨드 우선, Skill 없음
- 요청이 **React 컴포넌트/UI**이면 → frontend-design 또는 디자인 계열 Skill
- 요청이 **코드 품질/정리**이면 → simplify 또는 review
- 요청이 **설정/환경**이면 → update-config
- 패턴이 명확하지 않으면 → Skill 없이 직접 작업
- **Redis 연결 실패**: Redis 컨테이너 실행 확인, `REDIS_HOST`/`REDIS_PORT` 환경변수 확인
