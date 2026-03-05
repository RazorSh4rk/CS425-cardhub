package com.cardhub.service;

import com.cardhub.dto.TradeOfferRequest;
import com.cardhub.dto.TradeOfferResponse;
import com.cardhub.model.TradeOffer;
import com.cardhub.repository.TradeOfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeOfferService {

    private static final int MAX_ACTIVE_OFFERS = 5;

    private final TradeOfferRepository repository;

    public List<TradeOfferResponse> listActive() {
        return repository.findByStatusOrderByCreatedAtDesc(TradeOffer.Status.ACTIVE)
                .stream().map(TradeOfferResponse::from).toList();
    }

    public TradeOfferResponse getById(Long id) {
        return TradeOfferResponse.from(findOrThrow(id));
    }

    public TradeOfferResponse create(TradeOfferRequest request, Long creatorId) {
        long activeCount = repository.countByCreatorIdAndStatus(creatorId, TradeOffer.Status.ACTIVE);
        if (activeCount >= MAX_ACTIVE_OFFERS) {
            throw new IllegalArgumentException("Maximum " + MAX_ACTIVE_OFFERS + " active offers per user");
        }
        var offer = new TradeOffer();
        offer.setCreatorId(creatorId);
        offer.setTitle(request.title());
        offer.setDescription(request.description());
        offer.setContactPreferences(request.contactPreferences());
        return TradeOfferResponse.from(repository.save(offer));
    }

    public TradeOfferResponse update(Long id, TradeOfferRequest request, Long userId) {
        var offer = findOrThrow(id);
        if (!offer.getCreatorId().equals(userId)) {
            throw new IllegalArgumentException("Trade offer not found");
        }
        offer.setTitle(request.title());
        offer.setDescription(request.description());
        offer.setContactPreferences(request.contactPreferences());
        return TradeOfferResponse.from(repository.save(offer));
    }

    public void delete(Long id, Long userId) {
        var offer = findOrThrow(id);
        if (!offer.getCreatorId().equals(userId)) {
            throw new IllegalArgumentException("Trade offer not found");
        }
        repository.delete(offer);
    }

    private TradeOffer findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trade offer not found"));
    }
}
