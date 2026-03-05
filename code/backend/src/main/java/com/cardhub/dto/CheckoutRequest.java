package com.cardhub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CheckoutRequest(
        @NotBlank String contactName,
        @NotBlank @Email String contactEmail,
        String contactPhone
) {}
