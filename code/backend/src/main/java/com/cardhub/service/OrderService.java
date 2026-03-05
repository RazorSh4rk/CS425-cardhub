package com.cardhub.service;

import com.cardhub.dto.CheckoutRequest;
import com.cardhub.dto.OrderResponse;
import com.cardhub.dto.UpdateOrderStatusRequest;
import com.cardhub.model.Order;
import com.cardhub.model.OrderItem;
import com.cardhub.model.User;
import com.cardhub.repository.CardRepository;
import com.cardhub.repository.CartItemRepository;
import com.cardhub.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.08");

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final CardRepository cardRepository;

    @Transactional
    public OrderResponse checkout(Long customerId, CheckoutRequest request) {
        var cartItems = cartItemRepository.findByUserId(customerId);
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        var order = new Order();
        order.setCustomerId(customerId);
        order.setContactName(request.contactName());
        order.setContactEmail(request.contactEmail());
        order.setContactPhone(request.contactPhone());

        var subtotal = BigDecimal.ZERO;
        for (var cartItem : cartItems) {
            var card = cardRepository.findById(cartItem.getCard().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Card not found: " + cartItem.getCard().getName()));
            if (card.getQuantity() < cartItem.getQuantity()) {
                throw new IllegalArgumentException(
                        "Not enough stock for " + card.getName() + ". Available: " + card.getQuantity());
            }

            var item = new OrderItem();
            item.setOrder(order);
            item.setCardId(card.getId());
            item.setCardName(card.getName());
            item.setCardSet(card.getSet());
            item.setCondition(card.getCondition());
            item.setQuantity(cartItem.getQuantity());
            item.setPriceAtPurchase(card.getSellPrice());
            order.getItems().add(item);

            subtotal = subtotal.add(card.getSellPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));

            card.setQuantity(card.getQuantity() - cartItem.getQuantity());
            cardRepository.save(card);
        }

        var tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        order.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        order.setTax(tax);
        order.setTotal(subtotal.add(tax).setScale(2, RoundingMode.HALF_UP));

        log.info("MOCK - payment service called for ${} from {}", order.getTotal(), request.contactEmail());

        var saved = orderRepository.save(order);
        cartItemRepository.deleteByUserId(customerId);

        log.info("MOCK - email service called: order confirmation → {} for order #{}",
                request.contactEmail(), saved.getId());

        return OrderResponse.from(saved);
    }

    public List<OrderResponse> list(Long userId, String role) {
        if (User.Role.STAFF.name().equals(role) || User.Role.ADMIN.name().equals(role)) {
            return orderRepository.findAll().stream().map(OrderResponse::from).toList();
        }
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(userId)
                .stream().map(OrderResponse::from).toList();
    }

    public OrderResponse getById(Long id, Long userId, String role) {
        var order = findOrThrow(id);
        boolean isStaff = User.Role.STAFF.name().equals(role) || User.Role.ADMIN.name().equals(role);
        if (!isStaff && !order.getCustomerId().equals(userId)) {
            throw new IllegalArgumentException("Order not found");
        }
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse updateStatus(Long id, UpdateOrderStatusRequest request) {
        var order = findOrThrow(id);
        order.setStatus(request.status());
        return OrderResponse.from(orderRepository.save(order));
    }

    private Order findOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }
}
