package com.ecommerce.productservice.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum ProductDomainExceptionCode {

    // Product 관련 예외
    ProductNotFoundException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
    ProductAlreadyDeletedException(HttpStatus.BAD_REQUEST, "이미 삭제된 상품입니다."),
    InsufficientStockException(HttpStatus.BAD_REQUEST, "재고가 부족합니다."),
    InvalidPriceException(HttpStatus.BAD_REQUEST, "유효하지 않은 가격입니다."),
    InvalidQuantityException(HttpStatus.BAD_REQUEST, "유효하지 않은 수량입니다."),
    DuplicateProductNameException(HttpStatus.CONFLICT, "이미 존재하는 상품명입니다."),

    // Category 관련 예외
    CategoryNotFoundException(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."),
    DuplicateCategoryNameException(HttpStatus.CONFLICT, "이미 존재하는 카테고리명입니다."),
    InvalidCategoryParentException(HttpStatus.BAD_REQUEST, "자기 자신을 상위 카테고리로 지정할 수 없습니다."),

    // Cart 관련 예외
    CartItemNotFoundException(HttpStatus.NOT_FOUND, "장바구니 항목을 찾을 수 없습니다."),
    CartItemAlreadyExistsException(HttpStatus.CONFLICT, "이미 장바구니에 담긴 상품입니다."),

    // Wishlist 관련 예외
    WishlistItemNotFoundException(HttpStatus.NOT_FOUND, "찜 목록에서 상품을 찾을 수 없습니다."),
    WishlistItemAlreadyExistsException(HttpStatus.CONFLICT, "이미 찜한 상품입니다."),

    // JWT 관련 예외
    InvalidTokenException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    ExpiredTokenException(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    BlacklistedTokenException(HttpStatus.UNAUTHORIZED, "무효화된 토큰입니다."),
    AccessDeniedException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");

    final HttpStatus status;
    final String message;
}
