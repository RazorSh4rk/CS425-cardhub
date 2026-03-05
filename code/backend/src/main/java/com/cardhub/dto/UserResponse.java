package com.cardhub.dto;

import com.cardhub.model.User;
import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        String name,
        String role,
        boolean emailVerified,
        boolean accountEnabled,
        LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(), user.getEmail(), user.getName(),
                user.getRole().name(), user.isEmailVerified(),
                user.isAccountEnabled(), user.getCreatedAt()
        );
    }
}
