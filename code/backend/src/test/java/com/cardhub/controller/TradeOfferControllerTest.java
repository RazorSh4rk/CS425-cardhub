package com.cardhub.controller;

import com.cardhub.dto.TradeOfferRequest;
import com.cardhub.model.User;
import com.cardhub.repository.TradeOfferRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TradeOfferControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired TradeOfferRepository repository;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private String userToken;
    private String otherUserToken;

    @BeforeEach
    void setUp() throws Exception {
        repository.deleteAll();
        userRepository.deleteAll();
        userToken = loginAs("user@test.com", "password123", "User One", User.Role.CUSTOMER);
        otherUserToken = loginAs("other@test.com", "password123", "User Two", User.Role.CUSTOMER);
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

    private TradeOfferRequest basicOffer() {
        return new TradeOfferRequest("Looking for Charizard", "Have Blastoise, want Charizard", "Meet at store");
    }

    @Test
    void listOffers_withNoAuth_returns200() throws Exception {
        mockMvc.perform(get("/api/trades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void createOffer_returns200() throws Exception {
        mockMvc.perform(post("/api/trades")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicOffer())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Looking for Charizard")))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.expiresAt", notNullValue()));
    }

    @Test
    void createOffer_withNoAuth_returns501() throws Exception {
        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicOffer())))
                .andExpect(status().isNotImplemented());
    }

    @Test
    void getOffer_withNoAuth_returns200() throws Exception {
        var result = mockMvc.perform(post("/api/trades")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicOffer())))
                .andReturn();
        var id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/trades/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Looking for Charizard")));
    }

    @Test
    void updateOffer_byOwner_returns200() throws Exception {
        var result = mockMvc.perform(post("/api/trades")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicOffer())))
                .andReturn();
        var id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        var updated = new TradeOfferRequest("Updated Title", "Updated description", null);
        mockMvc.perform(put("/api/trades/" + id)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Title")));
    }

    @Test
    void updateOffer_byOtherUser_returnsBadRequest() throws Exception {
        var result = mockMvc.perform(post("/api/trades")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicOffer())))
                .andReturn();
        var id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        var updated = new TradeOfferRequest("Hacked Title", "Hacked", null);
        mockMvc.perform(put("/api/trades/" + id)
                        .header("Authorization", "Bearer " + otherUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Trade offer not found")));
    }

    @Test
    void deleteOffer_byOwner_returns204() throws Exception {
        var result = mockMvc.perform(post("/api/trades")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicOffer())))
                .andReturn();
        var id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/trades/" + id)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/trades"))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deleteOffer_byOtherUser_returnsBadRequest() throws Exception {
        var result = mockMvc.perform(post("/api/trades")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicOffer())))
                .andReturn();
        var id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/trades/" + id)
                        .header("Authorization", "Bearer " + otherUserToken))
                .andExpect(status().isBadRequest());
    }
}
