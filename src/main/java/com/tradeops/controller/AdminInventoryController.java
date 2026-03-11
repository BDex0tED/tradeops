package com.tradeops.controller;

import com.tradeops.model.entity.InventoryItem;
import com.tradeops.model.request.AdjustStockRequest;
import com.tradeops.model.response.InventoryItemResponse;
import com.tradeops.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/inventory")
@RequiredArgsConstructor
public class AdminInventoryController {

    private final InventoryService inventoryService;

    // FR-019: Просмотр остатков складовщиком
    @GetMapping
    public ResponseEntity<Page<InventoryItemResponse>> getInventory(Pageable pageable) {
        return ResponseEntity.ok(inventoryService.getInventoryList(pageable));
    }

    // FR-019: Изменение остатков
    @PatchMapping("/products/{productId}")
    public ResponseEntity<InventoryItemResponse> adjustStock(
            @PathVariable Long productId,
            @Valid @RequestBody AdjustStockRequest request) {

        return ResponseEntity.ok(inventoryService.adjustStock(productId, request.qtyOnHand()));
    }
}