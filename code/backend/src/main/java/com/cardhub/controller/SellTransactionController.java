package com.cardhub.controller;

import com.cardhub.dto.RejectRequest;
import com.cardhub.dto.SellQuoteRequest;
import com.cardhub.dto.SellQuoteResponse;
import com.cardhub.dto.SellTransactionResponse;
import com.cardhub.service.SellTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sell-transactions")
@RequiredArgsConstructor
public class SellTransactionController {

    private final SellTransactionService service;

    @PostMapping("/quote")
    public ResponseEntity<SellQuoteResponse> getQuote(@Valid @RequestBody SellQuoteRequest request) {
        return ResponseEntity.ok(service.getQuote(request));
    }

    @PostMapping
    public ResponseEntity<SellTransactionResponse> create(@Valid @RequestBody SellQuoteRequest request,
                                                          Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(service.create(request, userId));
    }

    @GetMapping
    public ResponseEntity<List<SellTransactionResponse>> list(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return ResponseEntity.ok(service.list(userId, role));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SellTransactionResponse> getById(@PathVariable Long id,
                                                            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return ResponseEntity.ok(service.getById(id, userId, role));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<SellTransactionResponse> complete(@PathVariable Long id,
                                                             Authentication authentication) {
        Long staffId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(service.complete(id, staffId));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<SellTransactionResponse> reject(@PathVariable Long id,
                                                           @Valid @RequestBody RejectRequest request,
                                                           Authentication authentication) {
        Long staffId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(service.reject(id, staffId, request));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
