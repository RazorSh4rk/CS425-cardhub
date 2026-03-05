package com.cardhub.controller;

import com.cardhub.dto.RejectRequest;
import com.cardhub.dto.SellQuoteRequest;
import com.cardhub.model.Card;
import com.cardhub.model.Condition;
import com.cardhub.model.User;
import com.cardhub.repository.SellTransactionRepository;
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
class SellTransactionControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired SellTransactionRepository repository;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private String customerToken;
    private String staffToken;

    @BeforeEach
    void setUp() throws Exception {
        repository.deleteAll();
        userRepository.deleteAll();
        customerToken = loginAs("customer@test.com", "password123", "Customer", User.Role.CUSTOMER);
        staffToken = loginAs("staff@test.com", "password123", "Staff", User.Role.STAFF);
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

    private SellQuoteRequest basicQuote() {
        return new SellQuoteRequest("Pikachu", "Base Set", Card.Game.POKEMON, Condition.MINT, 2);
    }

    @Test
    void getQuote_returnsCalculatedPrice() throws Exception {
        mockMvc.perform(post("/api/sell-transactions/quote")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicQuote())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardName", is("Pikachu")))
                .andExpect(jsonPath("$.quantity", is(2)))
                .andExpect(jsonPath("$.pricePerCard", notNullValue()))
                .andExpect(jsonPath("$.totalPrice", notNullValue()));
    }

    @Test
    void getQuote_withNoAuth_returns501() throws Exception {
        mockMvc.perform(post("/api/sell-transactions/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicQuote())))
                .andExpect(status().isNotImplemented());
    }

    @Test
    void createTransaction_asCustomer_returns200() throws Exception {
        mockMvc.perform(post("/api/sell-transactions")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicQuote())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardName", is("Pikachu")))
                .andExpect(jsonPath("$.status", is("PENDING_VERIFICATION")))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void listTransactions_customerSeesOwn() throws Exception {
        mockMvc.perform(post("/api/sell-transactions")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicQuote())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/sell-transactions")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void listTransactions_staffSeesAll() throws Exception {
        mockMvc.perform(post("/api/sell-transactions")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicQuote())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/sell-transactions")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void completeTransaction_asStaff_returns200() throws Exception {
        var result = mockMvc.perform(post("/api/sell-transactions")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicQuote())))
                .andReturn();
        var id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(patch("/api/sell-transactions/" + id + "/complete")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.staffId", notNullValue()));
    }

    @Test
    void completeTransaction_asCustomer_returns403() throws Exception {
        var result = mockMvc.perform(post("/api/sell-transactions")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicQuote())))
                .andReturn();
        var id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(patch("/api/sell-transactions/" + id + "/complete")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectTransaction_asStaff_returns200() throws Exception {
        var result = mockMvc.perform(post("/api/sell-transactions")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicQuote())))
                .andReturn();
        var id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        var reject = new RejectRequest("Card condition worse than stated");
        mockMvc.perform(patch("/api/sell-transactions/" + id + "/reject")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reject)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("REJECTED")))
                .andExpect(jsonPath("$.rejectionReason", is("Card condition worse than stated")));
    }

    @Test
    void completeAlreadyCompleted_returnsBadRequest() throws Exception {
        var result = mockMvc.perform(post("/api/sell-transactions")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicQuote())))
                .andReturn();
        var id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(patch("/api/sell-transactions/" + id + "/complete")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/sell-transactions/" + id + "/complete")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Transaction is not pending verification")));
    }
}
