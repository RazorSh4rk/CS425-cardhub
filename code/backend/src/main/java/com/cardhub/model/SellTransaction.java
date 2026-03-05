package com.cardhub.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sell_transactions")
public class SellTransaction {

    public enum Status { PENDING_VERIFICATION, COMPLETED, REJECTED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    private Long staffId;

    @Column(nullable = false)
    private String cardName;

    @Column(name = "card_set", nullable = false)
    private String cardSet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Card.Game cardGame;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Condition condition;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quotedPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private String rejectionReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = Status.PENDING_VERIFICATION;
    }
}
