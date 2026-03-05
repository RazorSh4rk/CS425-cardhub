package com.cardhub.controller;

import com.cardhub.dto.RegisterRequest;
import com.cardhub.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProtectedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private String validToken;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();

        var request = new RegisterRequest("protected@example.com", "password123", "Protected User");

        var result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        var json = objectMapper.readTree(result.getResponse().getContentAsString());
        validToken = json.get("token").asText();
    }

    @Test
    void example_withValidToken_returns200() throws Exception {
        mockMvc.perform(get("/api/protected/example")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Access granted")))
                .andExpect(jsonPath("$.userId", notNullValue()));
    }

    @Test
    void example_withNoToken_returns501() throws Exception {
        mockMvc.perform(get("/api/protected/example"))
                .andExpect(status().isNotImplemented())
                .andExpect(jsonPath("$.error", is("Invalid or missing authentication token")));
    }

    @Test
    void example_withInvalidToken_returns501() throws Exception {
        mockMvc.perform(get("/api/protected/example")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isNotImplemented())
                .andExpect(jsonPath("$.error", is("Invalid or missing authentication token")));
    }

    @Test
    void example_withMalformedAuthHeader_returns501() throws Exception {
        mockMvc.perform(get("/api/protected/example")
                        .header("Authorization", "NotBearer " + validToken))
                .andExpect(status().isNotImplemented());
    }

    @Test
    void example_withExpiredToken_returns501() throws Exception {
        mockMvc.perform(get("/api/protected/example")
                        .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJ0ZXN0QGV4YW1wbGUuY29tIiwicm9sZSI6IkNVU1RPTUVSIiwiaWF0IjoxNjAwMDAwMDAwLCJleHAiOjE2MDAwMDAwMDF9.invalid"))
                .andExpect(status().isNotImplemented());
    }

    @Test
    void example_withEmptyBearerToken_returns501() throws Exception {
        mockMvc.perform(get("/api/protected/example")
                        .header("Authorization", "Bearer "))
                .andExpect(status().isNotImplemented());
    }
}
