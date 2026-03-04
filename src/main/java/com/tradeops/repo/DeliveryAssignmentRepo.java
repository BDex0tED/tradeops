package com.tradeops.repo;

import com.tradeops.model.entity.DeliveryAssignment;
import com.tradeops.model.entity.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryAssignmentRepo extends JpaRepository<DeliveryAssignment, Long> {
    List<DeliveryAssignment> findByCourierIdAndStatusInOrderByIdDesc(Long courierId, List<DeliveryStatus> statuses);

    Optional<DeliveryAssignment> findByIdAndCourierId(Long id, Long courierId);
}
