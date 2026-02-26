package com.tradeops.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryRequest(
        @NotBlank(message = "Name is required")
        String name,
        Long parentId,
        String slug,
        Integer sortOrder
) {}
