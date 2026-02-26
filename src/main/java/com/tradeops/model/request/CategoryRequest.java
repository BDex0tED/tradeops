package com.tradeops.model.request;

public record CategoryRequest(
       String name,
       Long parentId,
       String slug,
       Integer sortOrder
) {}
