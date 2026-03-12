package com.tradeops.model.request;

import jakarta.validation.constraints.NotNull;

public record ToggleCourierStatusRequest(
        @NotNull(message = "Courier status is required")
        boolean status
) {
}
