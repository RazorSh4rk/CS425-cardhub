package com.cardhub.controller;

import com.cardhub.dto.TournamentRequest;
import com.cardhub.model.User;
import com.cardhub.repository.TournamentRepository;
import com.cardhub.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TournamentControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired TournamentRepository repository;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private String userToken;
    private String otherUserToken;

    @BeforeEach
    void setUp() throws Exception {
        repository.deleteAll();
        userRepository.deleteAll();
        userToken = loginAs("organizer@test.com", "password123", "Organizer", User.Role.CUSTOMER);
        otherUserToken = loginAs("other@test.com", "password123", "Other", User.Role.CUSTOMER);
    }

    private String loginAs(String email, String password, String name, User.Role role) throws Exception {
        var user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setName(name);
        user.setRole(role);
        userRepository.save(user);

        var body = objectMapper.writeValueAsString(new com.cardhub.dto.LoginRequest(email, password));
        var result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private TournamentRequest futureTournament() {
        return new TournamentRequest(
                "Friday Night Magic", LocalDate.now().plusDays(7),
                LocalTime.of(18, 0), LocalTime.of(22, 0),
                "Magic: The Gathering", "Standard",
                "Come play Standard format!"
        );
    }

    @Test
    void listTournaments_withNoAuth_returns200() throws Exception {
        mockMvc.perform(get("/api/tournaments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void createTournament_returns200() throws Exception {
        mockMvc.perform(post("/api/tournaments")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(futureTournament())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Friday Night Magic")))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.game", is("Magic: The Gathering")));
    }

    @Test
    void createTournament_withNoAuth_returns501() throws Exception {
        mockMvc.perform(post("/api/tournaments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(futureTournament())))
                .andExpect(status().isNotImplemented());
    }

    @Test
    void createTournament_withConflict_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/tournaments")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(futureTournament())))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/tournaments")
                        .header("Authorization", "Bearer " + otherUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(futureTournament())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("already reserved")));
    }

    @Test
    void getTournament_withNoAuth_returns200() throws Exception {
        var result = mockMvc.perform(post("/api/tournaments")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(futureTournament())))
                .andReturn();
        var id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/tournaments/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Friday Night Magic")));
    }

    @Test
    void checkAvailability_returnsAvailable() throws Exception {
        mockMvc.perform(get("/api/tournaments/availability")
                        .header("Authorization", "Bearer " + userToken)
                        .param("date", LocalDate.now().plusDays(14).toString())
                        .param("startTime", "18:00:00")
                        .param("endTime", "22:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available", is(true)));
    }

    @Test
    void checkAvailability_withConflict_returnsUnavailable() throws Exception {
        mockMvc.perform(post("/api/tournaments")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(futureTournament())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tournaments/availability")
                        .header("Authorization", "Bearer " + userToken)
                        .param("date", LocalDate.now().plusDays(7).toString())
                        .param("startTime", "19:00:00")
                        .param("endTime", "21:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available", is(false)));
    }

    @Test
    void cancelTournament_byOwner_returns204() throws Exception {
        var result = mockMvc.perform(post("/api/tournaments")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(futureTournament())))
                .andReturn();
        var id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/tournaments/" + id)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tournaments"))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void cancelTournament_byOtherUser_returnsBadRequest() throws Exception {
        var result = mockMvc.perform(post("/api/tournaments")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(futureTournament())))
                .andReturn();
        var id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/tournaments/" + id)
                        .header("Authorization", "Bearer " + otherUserToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Tournament not found")));
    }

    @Test
    void createTournament_withPastDate_returnsBadRequest() throws Exception {
        var pastTournament = new TournamentRequest(
                "Past Tournament", LocalDate.now().minusDays(1),
                LocalTime.of(18, 0), LocalTime.of(22, 0),
                "Pokemon", "Standard", null
        );
        mockMvc.perform(post("/api/tournaments")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pastTournament)))
                .andExpect(status().isBadRequest());
    }
}
