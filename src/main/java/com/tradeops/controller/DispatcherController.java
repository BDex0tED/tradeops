package com.tradeops.controller;

import com.tradeops.model.entity.DeliveryAssignment;
import com.tradeops.model.request.DeliveryAssignmentRequest;
import com.tradeops.model.response.DeliveryAssignmentResponse;
import com.tradeops.service.impl.DispatcherServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dispatch/assignments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_DISPATCHER', 'ROLE_SUPER_ADMIN')")
@Validated
public class DispatcherController {

    private final DispatcherServiceImpl dispatcherService;

    @PostMapping
    public ResponseEntity<DeliveryAssignmentResponse> assignOrder(
            @RequestBody @Valid DeliveryAssignmentRequest request) {
        return ResponseEntity.ok(dispatcherService.assignCourierToOrder(request.orderId(), request.courierId()));
    }
}
