package com.tradeops.repo;

import com.tradeops.model.entity.DeliveryAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryAssignmentRepo extends JpaRepository<DeliveryAssignment, Long> {
}
