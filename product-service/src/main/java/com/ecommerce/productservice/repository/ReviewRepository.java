package com.ecommerce.productservice.repository;

import com.ecommerce.productservice.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findAllByProductIdAndIsActiveTrue(Long productId, Pageable pageable);

    Page<Review> findAllByUserIdAndIsActiveTrue(Long userId, Pageable pageable);

    Optional<Review> findByIdAndIsActiveTrue(Long id);

    boolean existsByProductIdAndUserIdAndIsActiveTrue(Long productId, Long userId);
}
