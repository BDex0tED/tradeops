package com.tradeops.service.impl;

import com.tradeops.annotation.Auditable;
import com.tradeops.exceptions.InvalidStatusTransitionException;
import com.tradeops.exceptions.ResourceNotFoundException;
import com.tradeops.mapper.DeliveryAssignmentMapper;
import com.tradeops.model.entity.*;
import com.tradeops.model.response.DeliveryAssignmentResponse;
import com.tradeops.repo.CourierUserRepo;
import com.tradeops.repo.DeliveryAssignmentRepo;
import com.tradeops.repo.OrderRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DispatcherServiceImpl {

    private final OrderRepo orderRepo;
    private final CourierUserRepo courierUserRepo;
    private final DeliveryAssignmentRepo assignmentRepo;
    private final DeliveryAssignmentMapper assignmentMapper;

    @Transactional
    @Auditable(action = "COURIER_ASSIGNED", entityType = "DELIVERY_ASSIGNMENT")
    public DeliveryAssignmentResponse assignCourierToOrder(Long orderId, Long courierId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.NEW && order.getStatus() != OrderStatus.ASSIGNED) {
            throw new InvalidStatusTransitionException("Order must be NEW or ASSIGNED to be dispatched");
        }

        CourierUser courier = courierUserRepo.findById(courierId)
                .orElseThrow(() -> new ResourceNotFoundException("Courier not found"));

        order.setStatus(OrderStatus.ASSIGNED);
        orderRepo.save(order);

        DeliveryAssignment assignment = new DeliveryAssignment();
        assignment.setOrder(order);
        assignment.setCourier(courier);
        assignment.setStatus(DeliveryStatus.ASSIGNED);

        assignmentRepo.save(assignment);

        return assignmentMapper.toDeliveryAssignmentResponse(assignment);
    }
}
