package com.cardhub.dto;

import com.cardhub.model.User;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(@NotNull User.Role role) {}
