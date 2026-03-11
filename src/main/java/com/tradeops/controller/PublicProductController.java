package com.tradeops.controller;

import com.tradeops.model.entity.Product;
import com.tradeops.model.request.ProductDetailRequest;
import com.tradeops.model.request.ProductsRequest;
import com.tradeops.model.response.ProductResponse;
import com.tradeops.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
            @RequestBody ProductsRequest request,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
                .body(productService.getProducts(request.traderId(), request.categoryId(), request.query(), pageable));
    }

    // FR-016, FR-017
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductDetail(
            @PathVariable Long id,
            @RequestBody @Valid ProductDetailRequest request) {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
                .body(productService.getProductByIdAndTraderId(id, request.traderId()));
    }
}