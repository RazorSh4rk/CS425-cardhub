package com.cardhub.dto;

import com.cardhub.model.SellTransaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SellTransactionResponse(
        Long id,
        Long customerId,
        Long staffId,
        String cardName,
        String cardSet,
        String cardGame,
        String condition,
        int quantity,
        BigDecimal quotedPrice,
        String status,
        String rejectionReason,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {
    public static SellTransactionResponse from(SellTransaction t) {
        return new SellTransactionResponse(
                t.getId(), t.getCustomerId(), t.getStaffId(),
                t.getCardName(), t.getCardSet(), t.getCardGame().name(),
                t.getCondition().name(), t.getQuantity(), t.getQuotedPrice(),
                t.getStatus().name(), t.getRejectionReason(),
                t.getCreatedAt(), t.getCompletedAt()
        );
    }
}
