package com.cardhub.controller;

import com.cardhub.dto.TradeOfferRequest;
import com.cardhub.dto.TradeOfferResponse;
import com.cardhub.service.TradeOfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeOfferController {

    private final TradeOfferService service;

    @GetMapping
    public ResponseEntity<List<TradeOfferResponse>> listActive() {
        return ResponseEntity.ok(service.listActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TradeOfferResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<TradeOfferResponse> create(@Valid @RequestBody TradeOfferRequest request,
                                                      Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(service.create(request, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TradeOfferResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody TradeOfferRequest request,
                                                      Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(service.update(id, request, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        service.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
