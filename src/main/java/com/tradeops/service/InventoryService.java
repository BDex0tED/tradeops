package com.tradeops.service;

import com.tradeops.exceptions.ResourceNotFoundException;
import com.tradeops.model.entity.InventoryItem;
import com.tradeops.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryService {
    Page<InventoryItem> getInventoryList(Pageable pageable);

    InventoryItem adjustStock(Long productId, Integer newQtyOnHand);

    InventoryItem reserveStock(Long productId, Integer qtyToReserve);

    InventoryItem releaseStock(Long productId, Integer qtyToRelease);

    void fulfillStock(Long productId, Integer qtyToFulfill);

}
