package com.tradeops.controller;

import com.tradeops.model.entity.Order;
import com.tradeops.model.entity.OrderStatus;
import com.tradeops.repo.OrderRepo;
import com.tradeops.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
public class AdminOrdersController {

    private final OrderService orderService;
    private final OrderRepo orderRepo;

    // FR-025: Просмотр всех заказов компании (с фильтрацией)
    @GetMapping
    public ResponseEntity<Page<Order>> getOrders(Pageable pageable) {
        return ResponseEntity.ok(orderRepo.findAll(pageable));
    }

    // FR-026, FR-027: Смена статуса заказа
    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> changeStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {

        // В будущем тут будем доставать actorId из SecurityContext
        Long adminId = 1L;
        return ResponseEntity.ok(orderService.changeOrderStatus(id, status, adminId));
    }
}