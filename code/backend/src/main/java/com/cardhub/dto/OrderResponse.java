package com.cardhub.dto;

import com.cardhub.model.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long customerId,
        String contactName,
        String contactEmail,
        String contactPhone,
        String status,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal total,
        List<OrderItemResponse> items,
        LocalDateTime createdAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(), order.getCustomerId(),
                order.getContactName(), order.getContactEmail(), order.getContactPhone(),
                order.getStatus().name(),
                order.getSubtotal(), order.getTax(), order.getTotal(),
                order.getItems().stream().map(OrderItemResponse::from).toList(),
                order.getCreatedAt()
        );
    }
}
