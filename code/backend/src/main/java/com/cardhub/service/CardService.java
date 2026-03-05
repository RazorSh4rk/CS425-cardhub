package com.cardhub.service;

import com.cardhub.dto.CardRequest;
import com.cardhub.dto.CardResponse;
import com.cardhub.dto.PriceUpdateRequest;
import com.cardhub.model.Card;
import com.cardhub.model.Condition;
import com.cardhub.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;

    public List<CardResponse> search(String search, Card.Game game, Condition condition,
                                     BigDecimal minPrice, BigDecimal maxPrice) {
        return cardRepository.search(search, game, condition, minPrice, maxPrice)
                .stream().map(CardResponse::from).toList();
    }

    public CardResponse getById(Long id) {
        return CardResponse.from(findOrThrow(id));
    }

    public CardResponse create(CardRequest request, Long userId) {
        var card = new Card();
        applyRequest(card, request);
        card.setAddedByUserId(userId);
        return CardResponse.from(cardRepository.save(card));
    }

    public CardResponse update(Long id, CardRequest request, Long userId) {
        var card = findOrThrow(id);
        applyRequest(card, request);
        card.setAddedByUserId(userId);
        return CardResponse.from(cardRepository.save(card));
    }

    public CardResponse updatePrice(Long id, PriceUpdateRequest request) {
        var card = findOrThrow(id);
        card.setBuyPrice(request.buyPrice());
        card.setSellPrice(request.sellPrice());
        return CardResponse.from(cardRepository.save(card));
    }

    private Card findOrThrow(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));
    }

    private void applyRequest(Card card, CardRequest request) {
        card.setName(request.name());
        card.setSet(request.set());
        card.setGame(request.game());
        card.setCondition(request.condition());
        card.setQuantity(request.quantity());
        card.setFoil(request.foil());
        card.setImageUrl(request.imageUrl());
        card.setType(request.type());
        card.setRarity(request.rarity());
        card.setColor(request.color());
        card.setMarketPrice(request.marketPrice());
        card.setBuyPrice(request.buyPrice() != null ? request.buyPrice() : calculateBuyPrice(request));
        card.setSellPrice(request.sellPrice() != null ? request.sellPrice() : calculateSellPrice(request));
    }

    private BigDecimal calculateBuyPrice(CardRequest request) {
        return request.marketPrice()
                .multiply(BigDecimal.valueOf(0.60))
                .multiply(BigDecimal.valueOf(request.condition().priceMultiplier()))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateSellPrice(CardRequest request) {
        return request.marketPrice()
                .multiply(BigDecimal.valueOf(0.80))
                .multiply(BigDecimal.valueOf(request.condition().priceMultiplier()))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
