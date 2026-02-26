package com.tradeops.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Full name is required")
        String fullName,

        @Email
        @NotBlank(message = "Email is required")
        String email,

        @Size(min = 8)
        @NotBlank(message = "Password is required")
        String password,

        @Size(min = 8)
        @NotBlank(message = "Confirm password is required")
        String confirmPassword

) {}

