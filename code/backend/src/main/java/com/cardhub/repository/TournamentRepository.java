package com.cardhub.repository;

import com.cardhub.model.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    @Query("SELECT t FROM Tournament t WHERE t.status = 'ACTIVE' AND t.date >= :today ORDER BY t.date, t.startTime")
    List<Tournament> findUpcoming(@Param("today") LocalDate today);

    @Query("""
        SELECT COUNT(t) > 0 FROM Tournament t
        WHERE t.status = 'ACTIVE'
        AND t.date = :date
        AND t.startTime < :endTime
        AND t.endTime > :startTime
        AND (:excludeId IS NULL OR t.id <> :excludeId)
    """)
    boolean hasConflict(@Param("date") LocalDate date,
                        @Param("startTime") LocalTime startTime,
                        @Param("endTime") LocalTime endTime,
                        @Param("excludeId") Long excludeId);

    long countByOrganizerIdAndStatus(Long organizerId, Tournament.Status status);
}
