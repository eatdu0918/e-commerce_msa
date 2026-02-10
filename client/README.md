# Client — E-Commerce Frontend

MSA 기반 이커머스 프로젝트의 프론트엔드 애플리케이션입니다.

## 기술 스택

| 항목                | 기술                        |
| ------------------- | --------------------------- |
| **프레임워크**      | React 19 + TypeScript       |
| **빌드 도구**       | Vite 7                      |
| **스타일링**        | Tailwind CSS 4 + 커스텀 CSS |
| **HTTP 클라이언트** | Axios                       |
| **서버 상태 관리**  | TanStack React Query        |
| **국제화 (i18n)**   | i18next + react-i18next     |
| **아이콘**          | Lucide React                |
| **폰트**            | Google Fonts (Inter)        |

## 시작하기

```bash
# 의존성 설치
npm install

# 개발 서버 시작
npm run dev

# 프로덕션 빌드
npm run build
```

> 환경변수 설정은 `.env.example`을 참고하여 `.env` 파일을 생성하세요.

## 프로젝트 구조

```
client/
├── .env.example              # 환경변수 예시
├── Dockerfile                # Docker 빌드 설정
├── index.html                # SPA 엔트리 HTML
├── package.json              # 의존성 및 스크립트
├── postcss.config.js         # PostCSS 설정 (Tailwind 연동)
├── tailwind.config.js        # Tailwind CSS 설정
├── tsconfig.json             # TypeScript 설정 (루트)
├── tsconfig.app.json         # TypeScript 설정 (앱)
├── tsconfig.node.json        # TypeScript 설정 (Node)
├── vite.config.ts            # Vite 빌드/개발서버 설정
├── public/                   # 정적 파일 (상품 이미지 등)
└── src/                      # 소스 코드
```

### `src/` 소스 코드

```
src/
├── main.tsx                  # 앱 엔트리 포인트 (React DOM 렌더링)
├── App.tsx                   # 최상위 앱 컴포넌트 (상태 기반 라우팅)
├── index.css                 # 글로벌 CSS (Tailwind import, 커스텀 애니메이션)
├── i18n.ts                   # 다국어 설정 (한국어/영어)
├── demo.html                 # UI 프로토타입 / 데모 HTML
├── api/                      # API 통신 레이어
├── assets/                   # 정적 에셋
├── components/               # UI 컴포넌트
├── locales/                  # 다국어 번역 JSON
└── types/                    # TypeScript 타입 정의
```

### `src/api/` API 통신 레이어

```
api/
├── axios.ts                  # Axios 공통 인스턴스 (인터셉터 포함)
└── services/                 # 도메인별 API 서비스 모듈
    ├── cancel.ts             # 주문 취소 API
    ├── cart.ts               # 장바구니 API
    ├── category.ts           # 카테고리 API
    ├── coupon.ts             # 쿠폰 API
    ├── order.ts              # 주문 API
    ├── payment.ts            # 결제 API
    ├── product.ts            # 상품 API
    ├── refund.ts             # 환불 API
    ├── user.ts               # 유저 API
    └── wishlist.ts           # 위시리스트 API
```

- **`axios.ts`**: 공통 Axios 인스턴스
  - `baseURL`: 환경변수 `VITE_API_URL` 또는 기본 `http://localhost:8000` (Gateway)
  - **Request 인터셉터**: `sessionStorage`에서 `accessToken`을 읽어 `Authorization` 헤더에 자동 삽입
  - **Response 인터셉터**: 401 에러 발생 시 경고 처리

### `src/components/` UI 컴포넌트

기능 도메인별로 분류되어 있습니다.

```
components/
├── LanguageSwitcher.tsx       # 🌐 한/영 언어 전환
│
├── auth/                      # 🔐 인증
│   ├── LoginModal.tsx         # 로그인 모달
│   ├── SignupModal.tsx        # 회원가입 모달
│   └── SignupPage.tsx         # 회원가입 전체 페이지
│
├── cart/                      # 🛒 장바구니
│   └── CartModal.tsx          # 장바구니 모달
│
├── checkout/                  # 💳 결제
│   ├── CheckoutPage.tsx       # 결제 페이지
│   └── OrderCompletePage.tsx  # 주문 완료 페이지
│
├── home/                      # 🏠 홈 화면
│   ├── Hero.tsx               # 히어로 배너 섹션
│   └── CategoryGrid.tsx       # 카테고리 그리드
│
├── layout/                    # 🏗️ 레이아웃
│   ├── Header.tsx             # 헤더 / 네비게이션
│   └── Footer.tsx             # 푸터
│
├── order/                     # 📦 주문 관리
│   ├── OrderModal.tsx         # 주문 내역 모달
│   └── OrderDetailView.tsx    # 주문 상세 보기
│
├── product/                   # 🛍️ 상품
│   ├── ProductCard.tsx        # 상품 카드 컴포넌트
│   ├── ProductListPage.tsx    # 상품 목록 페이지
│   └── ProductDetailPage.tsx  # 상품 상세 페이지
│
└── profile/                   # 👤 마이페이지
    ├── MyPageView.tsx         # 마이페이지 메인
    ├── EditProfileView.tsx    # 프로필 편집
    ├── WishlistView.tsx       # 위시리스트
    ├── CouponView.tsx         # 쿠폰 관리
    ├── CancelRefundView.tsx   # 취소/환불 관리
    └── PaymentHistoryView.tsx # 결제 내역
```

### `src/types/` 타입 정의

```typescript
// product.ts
export interface Product {
    id: number;
    name: string;
    price: number;
    originalPrice?: number;
    image: string;
    rating: number;
    reviews: number;
    badge?: string;
    discount?: number;
    category: string;
    description: string;
    date: string;
}
```

### `src/locales/` 다국어 지원

```
locales/
├── en.json     # 영어 번역
└── ko.json     # 한국어 번역 (기본 언어)
```

### `public/` 정적 리소스

| 파일             | 설명                                                 |
| ---------------- | ---------------------------------------------------- |
| `vite.svg`       | Vite 로고                                            |
| `product1~4.png` | 기본 상품 이미지 4장                                 |
| `product_*.png`  | AI 생성 상품 이미지 12장 (가방, 재킷, 시계, 신발 등) |

## 아키텍처

```
┌─────────────┐    ┌─────────────────┐    ┌───────────────────┐
│  main.tsx   │───▶│  QueryClient    │───▶│     App.tsx       │
│  (엔트리)    │    │  Provider       │    │ (상태 기반 라우팅)  │
└─────────────┘    └─────────────────┘    └──────┬────────────┘
                                                 │
          ┌──────────────────────────────────────┤
          │                                      │
    ┌─────▼──────┐                     ┌─────────▼────────┐
    │  Layout    │                     │   Pages/Views    │
    │ Header     │                     │ Home, Product,   │
    │ Footer     │                     │ Checkout, Profile│
    └────────────┘                     └─────────┬────────┘
                                                 │
                                        ┌────────▼────────┐
                                        │   API Layer     │
                                        │ axios.ts +      │
                                        │ services/*.ts   │
                                        └────────┬────────┘
                                                 │
                                        ┌────────▼────────┐
                                        │  API Gateway    │
                                        │ localhost:8000  │
                                        └─────────────────┘
```

### 핵심 특징

1. **상태 기반 라우팅** — React Router를 사용하지 않고 `App.tsx`에서 `useState`로 뷰 전환 관리
2. **서버 상태 관리** — TanStack React Query를 활용한 데이터 페칭 및 캐싱
3. **도메인별 API 모듈** — 10개의 서비스 모듈로 백엔드 MSA 마이크로서비스와 통신
4. **다국어 지원** — i18next 기반 한국어/영어 지원 (기본 언어: 한국어)
5. **Gateway 통신** — 모든 API 호출은 Gateway(port 8000)를 통해 각 마이크로서비스로 라우팅
