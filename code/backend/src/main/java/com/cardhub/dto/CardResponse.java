package com.cardhub.dto;

import com.cardhub.model.Card;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CardResponse(
        Long id,
        String name,
        String set,
        String game,
        String condition,
        int quantity,
        boolean foil,
        String imageUrl,
        String type,
        String rarity,
        String color,
        BigDecimal marketPrice,
        BigDecimal buyPrice,
        BigDecimal sellPrice,
        LocalDateTime createdAt
) {
    public static CardResponse from(Card card) {
        return new CardResponse(
                card.getId(), card.getName(), card.getSet(),
                card.getGame().name(), card.getCondition().name(),
                card.getQuantity(), card.isFoil(), card.getImageUrl(),
                card.getType(), card.getRarity(), card.getColor(),
                card.getMarketPrice(), card.getBuyPrice(), card.getSellPrice(),
                card.getCreatedAt()
        );
    }
}
