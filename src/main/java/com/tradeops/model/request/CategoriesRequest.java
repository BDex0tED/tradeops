package com.tradeops.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record CategoriesRequest(
        @NotNull(message = "TraderId is required")
        @PositiveOrZero
        Long traderId,
        @PositiveOrZero
        @NotNull(message = "ParentId is required")
        Long parentId,
        @NotBlank(message = "Query is required")
        String query
) {}
