package com.tradeops.controller;

import com.tradeops.model.entity.Order;
import com.tradeops.repo.OrderRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trader/orders")
@RequiredArgsConstructor
public class TraderOrdersController {

    private final OrderRepo orderRepo;

    // FR-024: Владелец магазина видит ТОЛЬКО свои заказы (Tenant isolation)
    @GetMapping
    public ResponseEntity<Page<Order>> getTraderOrders(
            @RequestParam(name = "trader_id") Long traderId,
            Pageable pageable) {

        return ResponseEntity.ok(orderRepo.findByTraderId(traderId, pageable));
    }
}