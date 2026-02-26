package com.tradeops.repo;

import com.tradeops.model.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryItemRepo extends JpaRepository<InventoryItem, Long> {
    Optional<InventoryItem> findByProductId(Long productId);
}
