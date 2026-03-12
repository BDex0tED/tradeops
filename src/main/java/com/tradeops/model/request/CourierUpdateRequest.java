package com.tradeops.model.request;

import jakarta.validation.constraints.NotBlank;

public record CourierUpdateRequest(
        @NotBlank(message = "Name is required")
        String name,
        
        @NotBlank(message = "Phone is required")
        String phone,
        
        Boolean isActive
) {
}
