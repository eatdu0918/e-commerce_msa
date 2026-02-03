package com.ecommerce.productservice.repository;

import com.ecommerce.productservice.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndIsActiveTrue(Long id);

    Page<Product> findAllByIsActiveTrue(Pageable pageable);

    Page<Product> findAllByIsActiveTrueAndNameContaining(String name, Pageable pageable);

    Page<Product> findAllByIsActiveTrueAndCategoryId(Long categoryId, Pageable pageable);

    Page<Product> findAllByIsActiveTrueAndCategoryIdAndNameContaining(Long categoryId, String name, Pageable pageable);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
