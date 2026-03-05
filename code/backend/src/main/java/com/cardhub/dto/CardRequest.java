package com.cardhub.dto;

import com.cardhub.model.Card;
import com.cardhub.model.Condition;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CardRequest(
        @NotBlank String name,
        @NotBlank String set,
        @NotNull Card.Game game,
        @NotNull Condition condition,
        @Min(0) int quantity,
        boolean foil,
        String imageUrl,
        String type,
        String rarity,
        String color,
        @NotNull BigDecimal marketPrice,
        BigDecimal buyPrice,
        BigDecimal sellPrice
) {}
