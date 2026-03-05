package com.cardhub.dto;

import com.cardhub.model.OrderItem;
import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long cardId,
        String cardName,
        String cardSet,
        String condition,
        int quantity,
        BigDecimal priceAtPurchase,
        BigDecimal lineTotal
) {
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getId(), item.getCardId(), item.getCardName(), item.getCardSet(),
                item.getCondition().name(), item.getQuantity(), item.getPriceAtPurchase(),
                item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity()))
        );
    }
}
