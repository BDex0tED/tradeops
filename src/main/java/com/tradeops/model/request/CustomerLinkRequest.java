package com.tradeops.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CustomerLinkRequest(
        @NotNull(message = "TraderId required")
        Long traderId,

        @NotBlank(message = "customerExternalId required")
        String customerExternalId,

        @NotBlank(message = "CustomerPhone required")
        String customerPhone
) {}