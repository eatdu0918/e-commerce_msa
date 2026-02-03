package com.ecommerce.productservice.repository;

import com.ecommerce.productservice.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query("SELECT c FROM CartItem c JOIN FETCH c.product p WHERE c.userId = :userId AND p.isActive = true ORDER BY c.createdAt DESC")
    List<CartItem> findAllByUserIdWithProduct(@Param("userId") Long userId);

    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);

    Optional<CartItem> findByIdAndUserId(Long id, Long userId);

    void deleteAllByUserId(Long userId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    @Query("SELECT COUNT(c) FROM CartItem c WHERE c.userId = :userId")
    int countByUserId(@Param("userId") Long userId);
}
