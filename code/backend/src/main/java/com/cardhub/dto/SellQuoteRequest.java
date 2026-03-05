package com.cardhub.dto;

import com.cardhub.model.Card;
import com.cardhub.model.Condition;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SellQuoteRequest(
        @NotBlank String cardName,
        @NotBlank String cardSet,
        @NotNull Card.Game cardGame,
        @NotNull Condition condition,
        @Min(1) int quantity
) {}
