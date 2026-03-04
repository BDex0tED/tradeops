package com.tradeops.service.impl;

import com.tradeops.annotation.Auditable;
import com.tradeops.exceptions.InvalidStatusTransitionException;
import com.tradeops.exceptions.ResourceNotFoundException;
import com.tradeops.model.entity.*;
import com.tradeops.repo.CourierUserRepo;
import com.tradeops.repo.DeliveryAssignmentRepo;
import com.tradeops.repo.OrderRepo;
import com.tradeops.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourierServiceImpl {

    private final DeliveryAssignmentRepo assignmentRepo;
    private final CourierUserRepo courierUserRepo;
    private final OrderRepo orderRepo;
    private final InventoryService inventoryService;

    private CourierUser getCurrentCourier() {
        String phone = SecurityContextHolder.getContext().getAuthentication().getName();
        return courierUserRepo.findByPhone(phone)
                .orElseThrow(() -> new AccessDeniedException("Courier profile not found"));
    }

    public List<DeliveryAssignment> getActiveAssignments() {
        CourierUser courier = getCurrentCourier();
        return assignmentRepo.findByCourierIdAndStatusInOrderByIdDesc(
                courier.getId(),
                List.of(DeliveryStatus.ASSIGNED, DeliveryStatus.ON_PROGRESS));
    }

    @Transactional
    @Auditable(action = "DELIVERY_ACCEPTED", entityType = "DELIVERY_ASSIGNMENT")
    public DeliveryAssignment acceptAssignment(Long assignmentId) {
        CourierUser courier = getCurrentCourier();
        DeliveryAssignment assignment = assignmentRepo.findByIdAndCourierId(assignmentId, courier.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found or does not belong to you"));

        if (assignment.getStatus() != DeliveryStatus.ASSIGNED) {
            throw new InvalidStatusTransitionException("Assignment is not in ASSIGNED state");
        }

        assignment.setStatus(DeliveryStatus.ON_PROGRESS);
        assignment.setAcceptedAt(LocalDateTime.now());

        Order order = assignment.getOrder();
        order.setStatus(OrderStatus.ON_PROGRESS);
        orderRepo.save(order);

        return assignmentRepo.save(assignment);
    }

    @Transactional
    @Auditable(action = "DELIVERY_COMPLETED", entityType = "DELIVERY_ASSIGNMENT")
    public DeliveryAssignment completeAssignment(Long assignmentId) {
        CourierUser courier = getCurrentCourier();
        DeliveryAssignment assignment = assignmentRepo.findByIdAndCourierId(assignmentId, courier.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

        if (assignment.getStatus() != DeliveryStatus.ON_PROGRESS) {
            throw new InvalidStatusTransitionException("Assignment must be ON_PROGRESS to complete");
        }

        assignment.setStatus(DeliveryStatus.COMPLETED);

        Order order = assignment.getOrder();
        order.setStatus(OrderStatus.COMPLETED);
        orderRepo.save(order);

        for (OrderLine line : order.getOrderLines()) {
            inventoryService.fulfillStock(line.getProduct().getId(), line.getQty());
        }

        return assignmentRepo.save(assignment);
    }
}
