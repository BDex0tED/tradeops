package com.tradeops.controller;

import com.tradeops.model.entity.Order;
import com.tradeops.model.request.OrdersRequest;
import com.tradeops.model.response.OrderResponse;
import com.tradeops.repo.OrderRepo;
import com.tradeops.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trader/orders")
@RequiredArgsConstructor
public class TraderOrdersController {

    private final OrderService orderService;

    // FR-024:(Tenant isolation)
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_TRADER')")
    public ResponseEntity<Page<OrderResponse>> getTraderOrders(
            HttpServletRequest request,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long traderId = Long.parseLong(request.getHeader("X-Current-Trader-Id"));

        return ResponseEntity.ok(orderService.getAllOrders(traderId, pageable));
    }
}