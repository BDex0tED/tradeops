package com.tradeops.controller;

import com.tradeops.model.response.ProductResponse;
import com.tradeops.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/catalog/products")
@RequiredArgsConstructor
public class PublicProductController {

    private final ProductService productService;

    // FR-015, FR-017
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(name = "trader_id") Long traderId,
            @RequestParam(name = "category_id", required = false) Long categoryId,
            @RequestParam(name = "q", required = false) String query,
            Pageable pageable) {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
                .body(productService.getProducts(traderId, categoryId, query, pageable));
    }

    // FR-016, FR-017
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductDetail(
            @PathVariable Long id,
            @RequestParam(name = "trader_id") Long traderId) {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
                .body(productService.getProductByIdAndTraderId(id, traderId));
    }
}