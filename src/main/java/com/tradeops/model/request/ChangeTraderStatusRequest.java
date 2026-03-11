package com.tradeops.model.request;

import com.tradeops.model.entity.Trader;
import jakarta.validation.constraints.NotNull;

public record ChangeTraderStatusRequest(
        @NotNull(message = "Trader status is required")
        Trader.TraderStatus status
) {
}
