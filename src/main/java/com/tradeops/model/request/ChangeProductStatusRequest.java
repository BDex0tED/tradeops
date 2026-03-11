package com.tradeops.model.request;

import jakarta.validation.constraints.NotNull;

public record ChangeProductStatusRequest(
        @NotNull(message = "Product status is required")
        boolean isActive
) {
}
