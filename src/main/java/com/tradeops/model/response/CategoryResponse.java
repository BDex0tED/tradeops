package com.tradeops.model.response;

public record CategoryResponse(
        Long id,
        String name,
        String slug,
        Long parentId

) {}
