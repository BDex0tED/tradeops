package com.tradeops.controller;

import com.tradeops.model.request.CategoriesRequest;
import com.tradeops.model.response.CategoryResponse;
import com.tradeops.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/catalog/categories")
@RequiredArgsConstructor
public class PublicCatalogController {

    private final CategoryService categoryService;

    // FR-014, FR-017
    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> getCategories(
            @RequestBody @Valid CategoriesRequest request,
            Pageable pageable) {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
                .body(categoryService.getCategoriesByTraderParentIdsAndQuery(request.traderId(), request.parentId(), request.query(), pageable));
    }
}