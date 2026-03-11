package com.tradeops.model.response;

import com.tradeops.model.entity.Trader;

import java.time.LocalDateTime;

public record TraderResponse(
        Long id,
        String legalName,
        String displayName,
        String domain,
        LocalDateTime createdAt,
        String themeConfigJson,
        Trader.TraderStatus status

) {}
