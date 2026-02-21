package com.tradeops.model.response;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String role,
        Long userId
) {
}
