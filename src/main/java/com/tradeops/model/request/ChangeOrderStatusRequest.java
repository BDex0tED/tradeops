package com.tradeops.model.request;

import com.tradeops.model.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeOrderStatusRequest(
        @NotNull(message = "Order status is required")
        OrderStatus status
) {}