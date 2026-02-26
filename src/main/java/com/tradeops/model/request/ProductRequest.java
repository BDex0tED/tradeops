package com.tradeops.model.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record ProductRequest(
        @NotBlank(message = "SKU is required")
        String sku,
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 125, message = "Name must be between 2 and 125 characters")
        String name,
        String description,
        @NotNull(message = "Base price is required")
        @Positive(message = "Base price must be positive")
        BigDecimal basePrice,
        boolean isActive,
        @NotEmpty(message = "Images are required")
        List<String> images,
        @NotNull(message = "Category is required")
        Long categoryId
) {}
