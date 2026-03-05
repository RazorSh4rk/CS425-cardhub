package com.cardhub.dto;

import com.cardhub.model.TradeOffer;
import java.time.LocalDateTime;

public record TradeOfferResponse(
        Long id,
        Long creatorId,
        String title,
        String description,
        String contactPreferences,
        String status,
        LocalDateTime createdAt,
        LocalDateTime expiresAt
) {
    public static TradeOfferResponse from(TradeOffer offer) {
        return new TradeOfferResponse(
                offer.getId(), offer.getCreatorId(), offer.getTitle(),
                offer.getDescription(), offer.getContactPreferences(),
                offer.getStatus().name(), offer.getCreatedAt(), offer.getExpiresAt()
        );
    }
}
