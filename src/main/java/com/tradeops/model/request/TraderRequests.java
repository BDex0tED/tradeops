package com.tradeops.model.request;

import jakarta.validation.constraints.NotBlank;

public class TraderRequests {

    public record CreateTraderRequest(
            @NotBlank(message = "Legal name is required") String legalName,
            @NotBlank(message = "Display name is required") String displayName,
            @NotBlank(message = "Domain is required") String domain) {
    }

    public record UpdateTraderRequest(
            String legalName,
            String displayName,
            String domain) {
    }

    public record ThemeConfigRequest(
            @NotBlank(message = "Theme config cannot be empty") String themeConfigJson) {
    }

    public record CreatePersonnelRequest(
            @NotBlank String name,
            @NotBlank String email,
            @NotBlank String password,
            @NotBlank String role) {
    }
}
