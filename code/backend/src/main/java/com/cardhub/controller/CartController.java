package com.cardhub.controller;

import com.cardhub.dto.AddToCartRequest;
import com.cardhub.dto.CartItemResponse;
import com.cardhub.dto.UpdateCartItemRequest;
import com.cardhub.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<List<CartItemResponse>> getCart(Authentication authentication) {
        return ResponseEntity.ok(cartService.getCart(userId(authentication)));
    }

    @PostMapping("/items")
    public ResponseEntity<CartItemResponse> addItem(@Valid @RequestBody AddToCartRequest request,
                                                     Authentication authentication) {
        return ResponseEntity.ok(cartService.addItem(userId(authentication), request));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<CartItemResponse> updateItem(@PathVariable Long id,
                                                        @Valid @RequestBody UpdateCartItemRequest request,
                                                        Authentication authentication) {
        return ResponseEntity.ok(cartService.updateItem(id, userId(authentication), request));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> removeItem(@PathVariable Long id, Authentication authentication) {
        cartService.removeItem(id, userId(authentication));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        cartService.clearCart(userId(authentication));
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    private Long userId(Authentication auth) {
        return (Long) auth.getPrincipal();
    }
}
