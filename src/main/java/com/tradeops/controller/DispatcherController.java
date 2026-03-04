package com.tradeops.controller;

import com.tradeops.model.entity.DeliveryAssignment;
import com.tradeops.service.impl.DispatcherServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dispatch/assignments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_DISPATCHER', 'ROLE_SUPER_ADMIN')")
public class DispatcherController {

    private final DispatcherServiceImpl dispatcherService;

    @PostMapping
    public ResponseEntity<DeliveryAssignment> assignOrder(
            @RequestParam Long orderId,
            @RequestParam Long courierId) {
        return ResponseEntity.ok(dispatcherService.assignCourierToOrder(orderId, courierId));
    }
}
