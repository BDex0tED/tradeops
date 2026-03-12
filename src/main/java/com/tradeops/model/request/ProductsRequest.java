package com.tradeops.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductsRequest(
        @NotNull(message = "TraderId is required")
        @PositiveOrZero
        Long traderId,
        @PositiveOrZero
        Long categoryId,

        String query
) {
}
