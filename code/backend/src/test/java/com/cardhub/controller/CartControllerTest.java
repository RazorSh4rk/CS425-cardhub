package com.cardhub.controller;

import com.cardhub.dto.AddToCartRequest;
import com.cardhub.dto.UpdateCartItemRequest;
import com.cardhub.model.Card;
import com.cardhub.model.Condition;
import com.cardhub.model.User;
import com.cardhub.repository.CardRepository;
import com.cardhub.repository.CartItemRepository;
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
class CartControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CartItemRepository cartItemRepository;
    @Autowired CardRepository cardRepository;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private String token;
    private Long cardId;

    @BeforeEach
    void setUp() throws Exception {
        cartItemRepository.deleteAll();
        cardRepository.deleteAll();
        userRepository.deleteAll();
        token = loginAs("user@test.com", "password123", "User", User.Role.CUSTOMER);
        cardId = saveCard("Pikachu", 10, new BigDecimal("5.00")).getId();
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
                        .contentType(MediaType.APPLICATION_JSON).content(body)).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private Card saveCard(String name, int quantity, BigDecimal price) {
        var card = new Card();
        card.setName(name);
        card.setSet("Base Set");
        card.setGame(Card.Game.POKEMON);
        card.setCondition(Condition.NEAR_MINT);
        card.setQuantity(quantity);
        card.setMarketPrice(price);
        card.setBuyPrice(price.multiply(new BigDecimal("0.6")));
        card.setSellPrice(price);
        return cardRepository.save(card);
    }

    @Test
    void getCart_empty_returns200() throws Exception {
        mockMvc.perform(get("/api/cart").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void addItem_returns200() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddToCartRequest(cardId, 2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardName", is("Pikachu")))
                .andExpect(jsonPath("$.quantity", is(2)))
                .andExpect(jsonPath("$.lineTotal", notNullValue()));
    }

    @Test
    void addItem_sameTwice_combinesQuantity() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddToCartRequest(cardId, 2))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddToCartRequest(cardId, 3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(5)));
    }

    @Test
    void addItem_exceedsStock_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddToCartRequest(cardId, 99))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Not enough stock")));
    }

    @Test
    void updateItem_returns200() throws Exception {
        var result = mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddToCartRequest(cardId, 2))))
                .andReturn();
        var itemId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(put("/api/cart/items/" + itemId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateCartItemRequest(5))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(5)));
    }

    @Test
    void removeItem_returns204() throws Exception {
        var result = mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddToCartRequest(cardId, 2))))
                .andReturn();
        var itemId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/cart/items/" + itemId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/cart").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void clearCart_returns204() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddToCartRequest(cardId, 1))))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/cart").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/cart").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getCart_withNoAuth_returns501() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isNotImplemented());
    }
}
