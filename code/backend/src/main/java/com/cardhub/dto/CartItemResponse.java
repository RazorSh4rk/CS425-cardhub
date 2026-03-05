package com.cardhub.dto;

import com.cardhub.model.CartItem;
import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long cardId,
        String cardName,
        String cardSet,
        String game,
        String condition,
        boolean foil,
        String imageUrl,
        BigDecimal sellPrice,
        int quantity,
        BigDecimal lineTotal
) {
    public static CartItemResponse from(CartItem item) {
        var card = item.getCard();
        return new CartItemResponse(
                item.getId(), card.getId(), card.getName(), card.getSet(),
                card.getGame().name(), card.getCondition().name(), card.isFoil(),
                card.getImageUrl(), card.getSellPrice(), item.getQuantity(),
                card.getSellPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
        );
    }
}
