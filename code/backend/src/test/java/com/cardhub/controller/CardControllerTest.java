package com.cardhub.controller;

import com.cardhub.dto.CardRequest;
import com.cardhub.dto.PriceUpdateRequest;
import com.cardhub.dto.RegisterRequest;
import com.cardhub.model.Card;
import com.cardhub.model.Condition;
import com.cardhub.model.User;
import com.cardhub.repository.CardRepository;
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

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CardControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CardRepository cardRepository;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private String customerToken;
    private String staffToken;

    @BeforeEach
    void setUp() throws Exception {
        cardRepository.deleteAll();
        userRepository.deleteAll();
        customerToken = registerAndGetToken("customer@test.com", "password123", "Customer", User.Role.CUSTOMER);
        staffToken = registerAndGetToken("staff@test.com", "password123", "Staff", User.Role.STAFF);
    }

    private String registerAndGetToken(String email, String password, String name, User.Role role) throws Exception {
        var user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setName(name);
        user.setRole(role);
        userRepository.save(user);

        var loginBody = objectMapper.writeValueAsString(new com.cardhub.dto.LoginRequest(email, password));
        var result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private CardRequest basicCardRequest() {
        return new CardRequest("Charizard", "Base Set", Card.Game.POKEMON,
                Condition.NEAR_MINT, 5, false, null, "Fire", "Rare", "Red",
                new BigDecimal("100.00"), null, null);
    }

    @Test
    void listCards_withNoAuth_returns200() throws Exception {
        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void createCard_asStaff_returns200() throws Exception {
        mockMvc.perform(post("/api/cards")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicCardRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Charizard")))
                .andExpect(jsonPath("$.condition", is("NEAR_MINT")))
                .andExpect(jsonPath("$.quantity", is(5)))
                .andExpect(jsonPath("$.buyPrice", notNullValue()))
                .andExpect(jsonPath("$.sellPrice", notNullValue()));
    }

    @Test
    void createCard_asCustomer_returns403() throws Exception {
        mockMvc.perform(post("/api/cards")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicCardRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void createCard_withNoAuth_returns501() throws Exception {
        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicCardRequest())))
                .andExpect(status().isNotImplemented());
    }

    @Test
    void createCard_autocalculatesPrices() throws Exception {
        var result = mockMvc.perform(post("/api/cards")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicCardRequest())))
                .andExpect(status().isOk())
                .andReturn();

        var json = objectMapper.readTree(result.getResponse().getContentAsString());
        // NEAR_MINT (0.9): buy = 100 * 0.60 * 0.9 = 54.00, sell = 100 * 0.80 * 0.9 = 72.00
        assert new BigDecimal("54.00").compareTo(new BigDecimal(json.get("buyPrice").asText())) == 0;
        assert new BigDecimal("72.00").compareTo(new BigDecimal(json.get("sellPrice").asText())) == 0;
    }

    @Test
    void getCard_returns200() throws Exception {
        var createResult = mockMvc.perform(post("/api/cards")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicCardRequest())))
                .andReturn();
        var id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/cards/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) id)))
                .andExpect(jsonPath("$.name", is("Charizard")));
    }

    @Test
    void getCard_notFound_returns400() throws Exception {
        mockMvc.perform(get("/api/cards/9999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Card not found")));
    }

    @Test
    void searchCards_byName_returnsMatches() throws Exception {
        mockMvc.perform(post("/api/cards")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicCardRequest())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/cards?search=chariz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Charizard")));
    }

    @Test
    void searchCards_noMatch_returnsEmpty() throws Exception {
        mockMvc.perform(get("/api/cards?search=nomatch"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void updateCard_asStaff_returns200() throws Exception {
        var createResult = mockMvc.perform(post("/api/cards")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicCardRequest())))
                .andReturn();
        var id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        var updated = new CardRequest("Charizard Holo", "Base Set", Card.Game.POKEMON,
                Condition.MINT, 3, true, null, "Fire", "Holo Rare", "Red",
                new BigDecimal("200.00"), null, null);

        mockMvc.perform(put("/api/cards/" + id)
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Charizard Holo")))
                .andExpect(jsonPath("$.quantity", is(3)));
    }

    @Test
    void updatePrice_asStaff_returns200() throws Exception {
        var createResult = mockMvc.perform(post("/api/cards")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicCardRequest())))
                .andReturn();
        var id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        var priceUpdate = new PriceUpdateRequest(new BigDecimal("40.00"), new BigDecimal("60.00"));

        mockMvc.perform(patch("/api/cards/" + id + "/price")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(priceUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.buyPrice", is(40.00)))
                .andExpect(jsonPath("$.sellPrice", is(60.00)));
    }
}
