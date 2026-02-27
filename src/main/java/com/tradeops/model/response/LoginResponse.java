package com.tradeops.model.response;

import java.util.List;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        User user
) {}
