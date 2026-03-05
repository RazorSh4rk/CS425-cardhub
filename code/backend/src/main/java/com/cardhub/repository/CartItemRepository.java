package com.cardhub.repository;

import com.cardhub.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserId(Long userId);
    Optional<CartItem> findByUserIdAndCardId(Long userId, Long cardId);
    void deleteByUserId(Long userId);
}
