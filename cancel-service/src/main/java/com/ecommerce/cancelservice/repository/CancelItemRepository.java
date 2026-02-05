package com.ecommerce.cancelservice.repository;

import com.ecommerce.cancelservice.entity.CancelItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CancelItemRepository extends JpaRepository<CancelItem, Long> {

    List<CancelItem> findByCancelId(Long cancelId);
}
