package com.tradeops.model.response;
import com.tradeops.model.response.UserResponse;


public record RegisterResponse(String message,
                               UserResponse userResponse,
                               String accessToken,
                               String refreshToken) {}
