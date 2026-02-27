package com.tradeops.model.response;

import java.util.List;

public record User(
        Long id,
        String role,
        List<String> scope
){}
