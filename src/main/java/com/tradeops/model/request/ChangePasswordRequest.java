package com.tradeops.model.request;

public record ChangePasswordRequest(String oldPassword,
                                    String newPassword) {}
