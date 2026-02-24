package com.tradeops.repo;

import com.tradeops.model.entity.CustomerLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerLinkRepo extends JpaRepository<CustomerLink, Long> {
}
