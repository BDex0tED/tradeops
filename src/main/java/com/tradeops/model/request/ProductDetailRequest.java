package com.tradeops.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductDetailRequest(
        @NotNull(message = "Product id is required")
        @PositiveOrZero
        Long traderId
) {
}
