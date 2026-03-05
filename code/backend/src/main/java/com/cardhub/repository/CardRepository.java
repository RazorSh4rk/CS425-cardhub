package com.cardhub.repository;

import com.cardhub.model.Card;
import com.cardhub.model.Condition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {

    @Query("""
        SELECT c FROM Card c
        WHERE (:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))
                                OR LOWER(c.set) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:game IS NULL OR c.game = :game)
        AND (:condition IS NULL OR c.condition = :condition)
        AND (:minPrice IS NULL OR c.sellPrice >= :minPrice)
        AND (:maxPrice IS NULL OR c.sellPrice <= :maxPrice)
        ORDER BY c.name
    """)
    List<Card> search(
            @Param("search") String search,
            @Param("game") Card.Game game,
            @Param("condition") Condition condition,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );
}
