package com.ecommerce.productservice.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import com.ecommerce.common.exception.BaseExceptionCode;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum ProductDomainExceptionCode implements BaseExceptionCode {

    // Product 관련 예외 코드
    ProductNotFoundException(HttpStatus.NOT_FOUND, "상품 정보를 찾을 수 없습니다."),
    ProductAlreadyDeletedException(HttpStatus.BAD_REQUEST, "이미 삭제된 상품입니다."),
    InsufficientStockException(HttpStatus.BAD_REQUEST, "재고가 부족합니다."),
    InvalidPriceException(HttpStatus.BAD_REQUEST, "유효하지 않은 가격입니다."),
    InvalidQuantityException(HttpStatus.BAD_REQUEST, "유효하지 않은 수량입니다."),
    DuplicateProductNameException(HttpStatus.CONFLICT, "중복된 상품명이 존재합니다."),

    // Category 관련 예외 코드
    CategoryNotFoundException(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."),
    DuplicateCategoryNameException(HttpStatus.CONFLICT, "중복된 카테고리명이 존재합니다."),
    InvalidCategoryParentException(HttpStatus.BAD_REQUEST, "부적절한 상위 카테고리 설정입니다."),

    // Cart 관련 예외 코드
    CartItemNotFoundException(HttpStatus.NOT_FOUND, "장바구니 아이템을 찾을 수 없습니다."),
    CartItemAlreadyExistsException(HttpStatus.CONFLICT, "이미 장바구니에 존재하는 상품입니다."),

    // Wishlist 관련 예외 코드
    WishlistItemNotFoundException(HttpStatus.NOT_FOUND, "찜한 상품을 찾을 수 없습니다."),
    WishlistItemAlreadyExistsException(HttpStatus.CONFLICT, "이미 찜한 상품입니다."),

    // Review 관련 예외 코드
    ReviewNotFoundException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."),
    DuplicateReviewException(HttpStatus.CONFLICT, "이미 해당 주문에 대한 리뷰를 작성했습니다."),

    // JWT 관련 예외 코드
    InvalidTokenException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    ExpiredTokenException(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    BlacklistedTokenException(HttpStatus.UNAUTHORIZED, "사용이 중지된 토큰입니다."),
    AccessDeniedException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");

    final HttpStatus status;
    final String message;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
