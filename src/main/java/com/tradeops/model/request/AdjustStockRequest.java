package com.tradeops.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AdjustStockRequest(
        @NotNull(message = "Quantity is required")
        @Min(value = 0, message = "Quantity cannot be negative")
        Integer qtyOnHand
) {}