package com.cardhub.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cards")
public class Card {

    public enum Game { POKEMON, MAGIC_THE_GATHERING, YUGIOH, OTHER }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "card_set", nullable = false)
    private String set;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Game game;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Condition condition;

    @Column(nullable = false)
    private int quantity;

    private boolean foil;
    private String imageUrl;
    private String type;
    private String rarity;
    private String color;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal marketPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal buyPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal sellPrice;

    private Long addedByUserId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
