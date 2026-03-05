package com.cardhub.controller;

import com.cardhub.dto.AddToCartRequest;
import com.cardhub.dto.CheckoutRequest;
import com.cardhub.dto.UpdateOrderStatusRequest;
import com.cardhub.model.Card;
import com.cardhub.model.Condition;
import com.cardhub.model.Order;
import com.cardhub.model.User;
import com.cardhub.repository.CardRepository;
import com.cardhub.repository.CartItemRepository;
import com.cardhub.repository.OrderRepository;
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
class OrderControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired OrderRepository orderRepository;
    @Autowired CartItemRepository cartItemRepository;
    @Autowired CardRepository cardRepository;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private String customerToken;
    private String staffToken;
    private Long cardId;

    @BeforeEach
    void setUp() throws Exception {
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        cardRepository.deleteAll();
        userRepository.deleteAll();
        customerToken = loginAs("customer@test.com", "password123", "Customer", User.Role.CUSTOMER);
        staffToken = loginAs("staff@test.com", "password123", "Staff", User.Role.STAFF);
        cardId = saveCard("Charizard", 10, new BigDecimal("50.00")).getId();
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

    private void addToCart(String token, Long cardId, int qty) throws Exception {
        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddToCartRequest(cardId, qty))))
                .andExpect(status().isOk());
    }

    private CheckoutRequest basicCheckout() {
        return new CheckoutRequest("Levi Szabo", "levi@example.com", "555-1234");
    }

    @Test
    void checkout_emptCart_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicCheckout())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Cart is empty")));
    }

    @Test
    void checkout_createsOrder() throws Exception {
        addToCart(customerToken, cardId, 2);

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicCheckout())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PENDING_PICKUP")))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].cardName", is("Charizard")))
                .andExpect(jsonPath("$.items[0].quantity", is(2)))
                .andExpect(jsonPath("$.subtotal", is(100.00)))
                .andExpect(jsonPath("$.tax", is(8.00)))
                .andExpect(jsonPath("$.total", is(108.00)));
    }

    @Test
    void checkout_reducesInventory() throws Exception {
        addToCart(customerToken, cardId, 3);

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicCheckout())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/cards/" + cardId))
                .andExpect(jsonPath("$.quantity", is(7)));
    }

    @Test
    void checkout_clearsCart() throws Exception {
        addToCart(customerToken, cardId, 1);

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicCheckout())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/cart").header("Authorization", "Bearer " + customerToken))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void checkout_insufficientStock_returnsBadRequest() throws Exception {
        addToCart(customerToken, cardId, 5);
        saveCard("Charizard", 2, new BigDecimal("50.00"));

        var lowStockCard = cardRepository.findById(cardId).get();
        lowStockCard.setQuantity(2);
        cardRepository.save(lowStockCard);

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicCheckout())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Not enough stock")));
    }

    @Test
    void listOrders_customerSeesOwn() throws Exception {
        addToCart(customerToken, cardId, 1);
        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicCheckout())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/orders").header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void listOrders_staffSeesAll() throws Exception {
        addToCart(customerToken, cardId, 1);
        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicCheckout())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/orders").header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void updateOrderStatus_asStaff_returns200() throws Exception {
        addToCart(customerToken, cardId, 1);
        var result = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicCheckout())))
                .andReturn();
        var id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(patch("/api/orders/" + id + "/status")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateOrderStatusRequest(Order.Status.READY))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("READY")));
    }

    @Test
    void updateOrderStatus_asCustomer_returns403() throws Exception {
        addToCart(customerToken, cardId, 1);
        var result = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicCheckout())))
                .andReturn();
        var id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(patch("/api/orders/" + id + "/status")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateOrderStatusRequest(Order.Status.READY))))
                .andExpect(status().isForbidden());
    }
}
