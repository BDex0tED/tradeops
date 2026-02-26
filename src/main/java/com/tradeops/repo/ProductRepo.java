package com.tradeops.repo;

import com.tradeops.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepo extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE " +
            "p.category.id IN :allowedCategoryIds " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
            "AND (:query IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND p.isActive = true")
    Page<Product> findStorefrontProducts(
            @Param("allowedCategoryIds") List<Long> allowedCategoryIds,
            @Param("categoryId") Long categoryId,
            @Param("query") String query,
            Pageable pageable
    );
}