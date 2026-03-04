package com.tradeops.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradeops.annotation.Auditable;
import com.tradeops.exceptions.InsufficientStockException;
import com.tradeops.exceptions.ResourceNotFoundException;
import com.tradeops.model.entity.InventoryItem;
import com.tradeops.model.entity.Product;
import com.tradeops.repo.InventoryItemRepo;
import com.tradeops.repo.ProductRepo;
import com.tradeops.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryItemRepo inventoryItemRepo;
    private final ProductRepo productRepo;

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryItem> getInventoryList(Pageable pageable) {
        return inventoryItemRepo.findAll(pageable);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Auditable(action = "INVENTORY_ADJUST", entityType = "INVENTORY_ITEM")
    public InventoryItem adjustStock(Long productId, Integer newQtyOnHand) {
        if (newQtyOnHand < 0) {
            throw new IllegalArgumentException("Quantity on hand cannot be negative");
        }

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        InventoryItem item = inventoryItemRepo.findByProductId(productId)
                .orElseGet(() -> {
                    InventoryItem newItem = new InventoryItem();
                    newItem.setProduct(product);
                    newItem.setQtyOnHand(0);
                    newItem.setQtyReserved(0);
                    return newItem;
                });

        item.setQtyOnHand(newQtyOnHand);
        return inventoryItemRepo.save(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Auditable(action = "STOCK_RESERVED", entityType = "INVENTORY_ITEM")
    public InventoryItem reserveStock(Long productId, Integer qtyToReserve) {
        InventoryItem item = inventoryItemRepo.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));

        int available = item.getQtyOnHand() - item.getQtyReserved();
        if (qtyToReserve > available) {
            throw new InsufficientStockException("Not enough stock for product ID: " + productId);
        }

        item.setQtyReserved(item.getQtyReserved() + qtyToReserve);
        return inventoryItemRepo.save(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Auditable(action = "STOCK_RELEASED", entityType = "INVENTORY_ITEM")
    public InventoryItem releaseStock(Long productId, Integer qtyToRelease) {
        InventoryItem item = inventoryItemRepo.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));

        int newReserved = Math.max(0, item.getQtyReserved() - qtyToRelease);
        item.setQtyReserved(newReserved);
        return inventoryItemRepo.save(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Auditable(action = "STOCK_FULFILLED", entityType = "INVENTORY_ITEM")
    public void fulfillStock(Long productId, Integer qtyToFulfill) {
        InventoryItem item = inventoryItemRepo.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));

        item.setQtyOnHand(item.getQtyOnHand() - qtyToFulfill);
        item.setQtyReserved(item.getQtyReserved() - qtyToFulfill);

        inventoryItemRepo.save(item);
    }
}