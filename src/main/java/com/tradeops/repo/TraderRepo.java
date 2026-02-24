package com.tradeops.repo;

import com.tradeops.model.entity.Trader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TraderRepo extends JpaRepository<Trader, Long> {
}
