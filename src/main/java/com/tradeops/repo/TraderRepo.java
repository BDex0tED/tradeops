package com.tradeops.repo;

import com.tradeops.model.entity.Category;
import com.tradeops.model.entity.Trader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Repository
public interface TraderRepo extends JpaRepository<Trader, Long> {
    @Query("SELECT c FROM Trader t JOIN t.allowedCategoryIds c WHERE t.id = :traderId")
    List<Long> findCategoryIdsById(@Param("traderId") Long traderId);
}
