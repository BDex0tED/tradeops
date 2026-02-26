package com.tradeops.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradeops.exceptions.InsufficientStockException;
import com.tradeops.exceptions.ResourceNotFoundException;
import com.tradeops.model.entity.ActorType;
import com.tradeops.model.entity.AuditLog;
import com.tradeops.model.entity.InventoryItem;
import com.tradeops.model.entity.Product;
import com.tradeops.repo.AuditLogRepo;
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
    private final AuditLogRepo auditLogRepo;

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryItem> getInventoryList(Pageable pageable) {
        return inventoryItemRepo.findAll(pageable);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InventoryItem adjustStock(Long productId, Integer newQtyOnHand, Long actorId) {
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

        int oldQty = item.getQtyOnHand();
        item.setQtyOnHand(newQtyOnHand);

        InventoryItem savedItem = inventoryItemRepo.save(item);

        // FR-019 & FR-038: Запись в AuditLog
        saveAuditLog(actorId, "INVENTORY_ADJUST", savedItem.getId(),
                String.format("{\"old_qty_on_hand\": %d, \"new_qty_on_hand\": %d}", oldQty, newQtyOnHand));

        return savedItem;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reserveStock(Long productId, Integer qtyToReserve, Long actorId) {
        InventoryItem item = inventoryItemRepo.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));

        int available = item.getQtyOnHand() - item.getQtyReserved();
        if (qtyToReserve > available) {
            throw new InsufficientStockException("Not enough stock for product ID: " + productId);
        }

        int oldReserved = item.getQtyReserved();
        item.setQtyReserved(oldReserved + qtyToReserve);
        inventoryItemRepo.save(item);

        // FR-038: AuditLog
        saveAuditLog(actorId, "STOCK_RESERVED", item.getId(),
                String.format("{\"reserved_added\": %d}", qtyToReserve));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseStock(Long productId, Integer qtyToRelease, Long actorId) {
        InventoryItem item = inventoryItemRepo.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));

        int newReserved = Math.max(0, item.getQtyReserved() - qtyToRelease);
        item.setQtyReserved(newReserved);
        inventoryItemRepo.save(item);

        // FR-038: AuditLog
        saveAuditLog(actorId, "STOCK_RELEASED", item.getId(),
                String.format("{\"reserved_removed\": %d}", qtyToRelease));
    }

    private void saveAuditLog(Long actorId, String action, Long entityId, String diffJson) {
        AuditLog log = new AuditLog();
        log.setActorType(ActorType.COMPANY); // Пока хардкодим, потом можно брать из SecurityContext
        log.setActorId(actorId != null ? actorId : 1L); // ID админа или системы
        log.setAction(action);
        log.setEntityType("INVENTORY_ITEM");
        log.setEntityId(entityId);
        log.setDiffJson(diffJson);
        auditLogRepo.save(log);
    }
}