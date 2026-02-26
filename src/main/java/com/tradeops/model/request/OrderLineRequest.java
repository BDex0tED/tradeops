package com.tradeops.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderLineRequest(
        @NotNull(message = "Product id is required")
        Long productId,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity should be at least 1")
        Integer quantity
) {}