package com.cardhub.dto;

import com.cardhub.model.Tournament;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record TournamentResponse(
        Long id,
        Long organizerId,
        String name,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        String game,
        String format,
        String description,
        String status,
        LocalDateTime createdAt
) {
    public static TournamentResponse from(Tournament t) {
        return new TournamentResponse(
                t.getId(), t.getOrganizerId(), t.getName(),
                t.getDate(), t.getStartTime(), t.getEndTime(),
                t.getGame(), t.getFormat(), t.getDescription(),
                t.getStatus().name(), t.getCreatedAt()
        );
    }
}
