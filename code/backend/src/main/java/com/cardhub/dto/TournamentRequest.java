package com.cardhub.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record TournamentRequest(
        @NotBlank String name,
        @NotNull @Future LocalDate date,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        @NotBlank String game,
        @NotBlank String format,
        String description
) {}
