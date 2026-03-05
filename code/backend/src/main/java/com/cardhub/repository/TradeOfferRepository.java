package com.cardhub.repository;

import com.cardhub.model.TradeOffer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeOfferRepository extends JpaRepository<TradeOffer, Long> {
    List<TradeOffer> findByStatusOrderByCreatedAtDesc(TradeOffer.Status status);
    long countByCreatorIdAndStatus(Long creatorId, TradeOffer.Status status);
}
