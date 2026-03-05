package com.cardhub.service;

import com.cardhub.dto.RejectRequest;
import com.cardhub.dto.SellQuoteRequest;
import com.cardhub.dto.SellQuoteResponse;
import com.cardhub.dto.SellTransactionResponse;
import com.cardhub.model.SellTransaction;
import com.cardhub.model.User;
import com.cardhub.repository.SellTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellTransactionService {

    private static final BigDecimal MOCK_MARKET_PRICE = new BigDecimal("10.00");
    private static final BigDecimal BUY_RATE = new BigDecimal("0.60");

    private final SellTransactionRepository repository;

    public SellQuoteResponse getQuote(SellQuoteRequest request) {
        log.info("MOCK - card price service called for: {} ({})", request.cardName(), request.cardGame());
        var pricePerCard = MOCK_MARKET_PRICE
                .multiply(BUY_RATE)
                .multiply(BigDecimal.valueOf(request.condition().priceMultiplier()))
                .setScale(2, RoundingMode.HALF_UP);
        var total = pricePerCard.multiply(BigDecimal.valueOf(request.quantity())).setScale(2, RoundingMode.HALF_UP);
        return new SellQuoteResponse(
                request.cardName(), request.cardSet(), request.cardGame().name(),
                request.condition().name(), request.quantity(), pricePerCard, total
        );
    }

    public SellTransactionResponse create(SellQuoteRequest request, Long customerId) {
        var quote = getQuote(request);
        var tx = new SellTransaction();
        tx.setCustomerId(customerId);
        tx.setCardName(request.cardName());
        tx.setCardSet(request.cardSet());
        tx.setCardGame(request.cardGame());
        tx.setCondition(request.condition());
        tx.setQuantity(request.quantity());
        tx.setQuotedPrice(quote.totalPrice());
        return SellTransactionResponse.from(repository.save(tx));
    }

    public List<SellTransactionResponse> list(Long userId, String role) {
        if (User.Role.STAFF.name().equals(role) || User.Role.ADMIN.name().equals(role)) {
            return repository.findAll().stream().map(SellTransactionResponse::from).toList();
        }
        return repository.findByCustomerIdOrderByCreatedAtDesc(userId)
                .stream().map(SellTransactionResponse::from).toList();
    }

    public SellTransactionResponse getById(Long id, Long userId, String role) {
        var tx = findOrThrow(id);
        boolean isStaff = User.Role.STAFF.name().equals(role) || User.Role.ADMIN.name().equals(role);
        if (!isStaff && !tx.getCustomerId().equals(userId)) {
            throw new IllegalArgumentException("Transaction not found");
        }
        return SellTransactionResponse.from(tx);
    }

    public SellTransactionResponse complete(Long id, Long staffId) {
        var tx = findOrThrow(id);
        if (tx.getStatus() != SellTransaction.Status.PENDING_VERIFICATION) {
            throw new IllegalArgumentException("Transaction is not pending verification");
        }
        tx.setStatus(SellTransaction.Status.COMPLETED);
        tx.setStaffId(staffId);
        tx.setCompletedAt(LocalDateTime.now());
        return SellTransactionResponse.from(repository.save(tx));
    }

    public SellTransactionResponse reject(Long id, Long staffId, RejectRequest request) {
        var tx = findOrThrow(id);
        if (tx.getStatus() != SellTransaction.Status.PENDING_VERIFICATION) {
            throw new IllegalArgumentException("Transaction is not pending verification");
        }
        tx.setStatus(SellTransaction.Status.REJECTED);
        tx.setStaffId(staffId);
        tx.setRejectionReason(request.reason());
        tx.setCompletedAt(LocalDateTime.now());
        return SellTransactionResponse.from(repository.save(tx));
    }

    private SellTransaction findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
    }
}
