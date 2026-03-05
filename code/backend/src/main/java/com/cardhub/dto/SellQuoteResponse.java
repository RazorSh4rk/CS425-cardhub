package com.cardhub.dto;

import java.math.BigDecimal;

public record SellQuoteResponse(
        String cardName,
        String cardSet,
        String cardGame,
        String condition,
        int quantity,
        BigDecimal pricePerCard,
        BigDecimal totalPrice
) {}
