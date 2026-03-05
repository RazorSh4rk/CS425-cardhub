package com.cardhub.service;

import com.cardhub.dto.AddToCartRequest;
import com.cardhub.dto.CartItemResponse;
import com.cardhub.dto.UpdateCartItemRequest;
import com.cardhub.model.CartItem;
import com.cardhub.repository.CardRepository;
import com.cardhub.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final CardRepository cardRepository;

    public List<CartItemResponse> getCart(Long userId) {
        return cartItemRepository.findByUserId(userId)
                .stream().map(CartItemResponse::from).toList();
    }

    public CartItemResponse addItem(Long userId, AddToCartRequest request) {
        var card = cardRepository.findById(request.cardId())
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));

        if (card.getQuantity() < request.quantity()) {
            throw new IllegalArgumentException("Not enough stock. Available: " + card.getQuantity());
        }

        var existing = cartItemRepository.findByUserIdAndCardId(userId, card.getId());
        if (existing.isPresent()) {
            var item = existing.get();
            int newQty = item.getQuantity() + request.quantity();
            if (card.getQuantity() < newQty) {
                throw new IllegalArgumentException("Not enough stock. Available: " + card.getQuantity());
            }
            item.setQuantity(newQty);
            return CartItemResponse.from(cartItemRepository.save(item));
        }

        var item = new CartItem();
        item.setUserId(userId);
        item.setCard(card);
        item.setQuantity(request.quantity());
        return CartItemResponse.from(cartItemRepository.save(item));
    }

    public CartItemResponse updateItem(Long itemId, Long userId, UpdateCartItemRequest request) {
        var item = findOwnedItem(itemId, userId);
        if (item.getCard().getQuantity() < request.quantity()) {
            throw new IllegalArgumentException("Not enough stock. Available: " + item.getCard().getQuantity());
        }
        item.setQuantity(request.quantity());
        return CartItemResponse.from(cartItemRepository.save(item));
    }

    public void removeItem(Long itemId, Long userId) {
        var item = findOwnedItem(itemId, userId);
        cartItemRepository.delete(item);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    private CartItem findOwnedItem(Long itemId, Long userId) {
        var item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));
        if (!item.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Cart item not found");
        }
        return item;
    }
}
