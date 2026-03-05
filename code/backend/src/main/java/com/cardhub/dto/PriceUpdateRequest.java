package com.cardhub.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PriceUpdateRequest(@NotNull BigDecimal buyPrice, @NotNull BigDecimal sellPrice) {}
