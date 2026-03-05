package com.cardhub.dto;

import com.cardhub.model.Order;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(@NotNull Order.Status status) {}
