package com.tradeops.controller;

import com.tradeops.model.entity.Order;
import com.tradeops.model.request.CreateOrderRequest;
import com.tradeops.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/storefront/orders")
@RequiredArgsConstructor
public class StoreFrontOrdersController {

    private final OrderService orderService;

    // FR-023: Создание заказа с витрины магазина
    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
}