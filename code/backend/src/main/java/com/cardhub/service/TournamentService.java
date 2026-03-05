package com.cardhub.service;

import com.cardhub.dto.TournamentRequest;
import com.cardhub.dto.TournamentResponse;
import com.cardhub.model.Tournament;
import com.cardhub.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TournamentService {

    private static final int MAX_ACTIVE_TOURNAMENTS = 2;

    private final TournamentRepository repository;

    public List<TournamentResponse> listUpcoming() {
        return repository.findUpcoming(LocalDate.now())
                .stream().map(TournamentResponse::from).toList();
    }

    public TournamentResponse getById(Long id) {
        return TournamentResponse.from(findOrThrow(id));
    }

    public Map<String, Object> checkAvailability(LocalDate date, java.time.LocalTime startTime,
                                                  java.time.LocalTime endTime) {
        boolean available = !repository.hasConflict(date, startTime, endTime, null);
        return Map.of("available", available, "date", date.toString(),
                "startTime", startTime.toString(), "endTime", endTime.toString());
    }

    public TournamentResponse create(TournamentRequest request, Long organizerId) {
        if (request.startTime().isAfter(request.endTime()) || request.startTime().equals(request.endTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        if (repository.hasConflict(request.date(), request.startTime(), request.endTime(), null)) {
            throw new IllegalArgumentException("Tournament room is already reserved for this time");
        }
        long activeCount = repository.countByOrganizerIdAndStatus(organizerId, Tournament.Status.ACTIVE);
        if (activeCount >= MAX_ACTIVE_TOURNAMENTS) {
            throw new IllegalArgumentException("Maximum " + MAX_ACTIVE_TOURNAMENTS + " active tournament posts per user");
        }
        var t = new Tournament();
        t.setOrganizerId(organizerId);
        t.setName(request.name());
        t.setDate(request.date());
        t.setStartTime(request.startTime());
        t.setEndTime(request.endTime());
        t.setGame(request.game());
        t.setFormat(request.format());
        t.setDescription(request.description());
        return TournamentResponse.from(repository.save(t));
    }

    public void cancel(Long id, Long userId) {
        var t = findOrThrow(id);
        if (!t.getOrganizerId().equals(userId)) {
            throw new IllegalArgumentException("Tournament not found");
        }
        t.setStatus(Tournament.Status.ARCHIVED);
        repository.save(t);
    }

    private Tournament findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));
    }
}
