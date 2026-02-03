package com.ecommerce.productservice.repository;

import com.ecommerce.productservice.entity.WishlistItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    @Query("SELECT w FROM WishlistItem w JOIN FETCH w.product p WHERE w.userId = :userId AND p.isActive = true ORDER BY w.createdAt DESC")
    Page<WishlistItem> findAllByUserIdWithProduct(@Param("userId") Long userId, Pageable pageable);

    Optional<WishlistItem> findByUserIdAndProductId(Long userId, Long productId);

    Optional<WishlistItem> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    @Query("SELECT COUNT(w) FROM WishlistItem w WHERE w.userId = :userId")
    int countByUserId(@Param("userId") Long userId);

    void deleteAllByUserId(Long userId);
}
