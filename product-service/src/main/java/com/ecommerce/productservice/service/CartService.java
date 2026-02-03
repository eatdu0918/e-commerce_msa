package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.request.AddCartItemRequest;
import com.ecommerce.productservice.dto.request.UpdateCartItemRequest;
import com.ecommerce.productservice.dto.response.CartItemResponse;
import com.ecommerce.productservice.dto.response.CartResponse;
import com.ecommerce.productservice.entity.CartItem;
import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.exception.ProductDomainException;
import com.ecommerce.productservice.exception.ProductDomainExceptionCode;
import com.ecommerce.productservice.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findAllByUserIdWithProduct(userId);
        List<CartItemResponse> items = cartItems.stream()
                .map(CartItemResponse::from)
                .collect(Collectors.toList());
        return CartResponse.from(items);
    }

    @Transactional
    public CartItemResponse addToCart(Long userId, AddCartItemRequest request) {
        log.info("장바구니 추가 시도: userId={}, productId={}", userId, request.getProductId());

        Product product = productService.getActiveProduct(request.getProductId());

        // 이미 장바구니에 있는 상품인지 확인
        Optional<CartItem> existingItem = cartItemRepository.findByUserIdAndProductId(userId, request.getProductId());

        if (existingItem.isPresent()) {
            // 이미 있으면 수량 증가
            CartItem cartItem = existingItem.get();
            cartItem.increaseQuantity(request.getQuantity());
            log.info("장바구니 수량 증가: cartItemId={}, newQuantity={}", cartItem.getId(), cartItem.getQuantity());
            return CartItemResponse.from(cartItem);
        }

        // 새로 추가
        CartItem cartItem = CartItem.create(userId, product, request.getQuantity());
        CartItem savedItem = cartItemRepository.save(cartItem);

        log.info("장바구니 추가 완료: cartItemId={}", savedItem.getId());
        return CartItemResponse.from(savedItem);
    }

    @Transactional
    public CartItemResponse updateCartItem(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        log.info("장바구니 수량 수정: userId={}, cartItemId={}", userId, cartItemId);

        CartItem cartItem = cartItemRepository.findByIdAndUserId(cartItemId, userId)
                .orElseThrow(() -> new ProductDomainException(ProductDomainExceptionCode.CartItemNotFoundException));

        cartItem.updateQuantity(request.getQuantity());

        log.info("장바구니 수량 수정 완료: cartItemId={}, newQuantity={}", cartItemId, request.getQuantity());
        return CartItemResponse.from(cartItem);
    }

    @Transactional
    public void removeFromCart(Long userId, Long cartItemId) {
        log.info("장바구니 항목 삭제: userId={}, cartItemId={}", userId, cartItemId);

        CartItem cartItem = cartItemRepository.findByIdAndUserId(cartItemId, userId)
                .orElseThrow(() -> new ProductDomainException(ProductDomainExceptionCode.CartItemNotFoundException));

        cartItemRepository.delete(cartItem);
        log.info("장바구니 항목 삭제 완료: cartItemId={}", cartItemId);
    }

    @Transactional
    public void clearCart(Long userId) {
        log.info("장바구니 비우기: userId={}", userId);
        cartItemRepository.deleteAllByUserId(userId);
        log.info("장바구니 비우기 완료: userId={}", userId);
    }

    @Transactional(readOnly = true)
    public int getCartItemCount(Long userId) {
        return cartItemRepository.countByUserId(userId);
    }
}
