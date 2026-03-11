package com.tradeops.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DeliveryAssignmentRequest(
        @NotNull(message = "Order id is required")
        @Positive
        Long orderId,

        @NotNull(message = "Courier id is required")
        @Positive
        Long courierId
) {
}
