package com.tradeops.repo;

import com.tradeops.model.entity.CourierUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourierUserRepo extends JpaRepository<CourierUser, Long> {
    Optional<CourierUser> findByPhone(String phone);
}
