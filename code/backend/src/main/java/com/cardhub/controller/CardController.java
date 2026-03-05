package com.cardhub.controller;

import com.cardhub.dto.CardRequest;
import com.cardhub.dto.CardResponse;
import com.cardhub.dto.PriceUpdateRequest;
import com.cardhub.model.Card;
import com.cardhub.model.Condition;
import com.cardhub.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping
    public ResponseEntity<List<CardResponse>> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Card.Game game,
            @RequestParam(required = false) Condition condition,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return ResponseEntity.ok(cardService.search(search, game, condition, minPrice, maxPrice));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<CardResponse> create(@Valid @RequestBody CardRequest request,
                                               Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(cardService.create(request, userId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<CardResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody CardRequest request,
                                               Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(cardService.update(id, request, userId));
    }

    @PatchMapping("/{id}/price")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<CardResponse> updatePrice(@PathVariable Long id,
                                                    @Valid @RequestBody PriceUpdateRequest request) {
        return ResponseEntity.ok(cardService.updatePrice(id, request));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
