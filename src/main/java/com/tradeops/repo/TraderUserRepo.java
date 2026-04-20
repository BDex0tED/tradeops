package com.tradeops.repo;

import com.tradeops.model.entity.TraderUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TraderUserRepo extends JpaRepository<TraderUser, Long> {
    Optional<TraderUser> findByEmail(String email);
    @Query("SELECT t.id FROM TraderUser t WHERE t.email = :email")
    Optional<Long> findIdByEmail(String email);
}
