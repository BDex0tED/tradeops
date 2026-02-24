package com.tradeops.repo;

import com.tradeops.model.entity.TraderUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TraderUserRepo extends JpaRepository<TraderUser, Long> {
}
