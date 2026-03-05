package com.cardhub.controller;

import com.cardhub.dto.CheckoutRequest;
import com.cardhub.dto.OrderResponse;
import com.cardhub.dto.UpdateOrderStatusRequest;
import com.cardhub.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> checkout(@Valid @RequestBody CheckoutRequest request,
                                                   Authentication authentication) {
        return ResponseEntity.ok(orderService.checkout(userId(authentication), request));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> list(Authentication authentication) {
        return ResponseEntity.ok(orderService.list(userId(authentication), role(authentication)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(orderService.getById(id, userId(authentication), role(authentication)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id,
                                                       @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateStatus(id, request));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    private Long userId(Authentication auth) {
        return (Long) auth.getPrincipal();
    }

    private String role(Authentication auth) {
        return auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
    }
}
