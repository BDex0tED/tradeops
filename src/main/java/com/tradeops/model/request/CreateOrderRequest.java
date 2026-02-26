package com.tradeops.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
        @NotNull(message = "TraderId is required")
        Long traderId,

        @NotBlank(message = "Address is required")
        String deliveryAddress,

        @NotBlank(message = "Phone number is required")
        String customerPhone,

        @NotBlank(message = "Payment method is only COD")
        String paymentMethod,

        @NotEmpty(message = "Lines can't be empty")
        @Valid
        List<OrderLineRequest> lines
) {}