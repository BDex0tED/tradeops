package com.tradeops.repo;

import com.tradeops.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepo extends JpaRepository<Order, Long> {
    Page<Order> findByTraderId(Long traderId, Pageable pageable);
}
