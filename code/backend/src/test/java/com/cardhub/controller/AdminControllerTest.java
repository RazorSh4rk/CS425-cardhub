package com.cardhub.controller;

import com.cardhub.dto.ChangeRoleRequest;
import com.cardhub.model.User;
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
class AdminControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private String adminToken;
    private String customerToken;
    private Long targetUserId;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        adminToken = createAndLogin("admin@test.com", "password123", "Admin", User.Role.ADMIN);
        customerToken = createAndLogin("customer@test.com", "password123", "Customer", User.Role.CUSTOMER);
        targetUserId = userRepository.findByEmail("customer@test.com").get().getId();
    }

    private String createAndLogin(String email, String password, String name, User.Role role) throws Exception {
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

    @Test
    void listUsers_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void listUsers_asCustomer_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void listUsers_withNoAuth_returns501() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isNotImplemented());
    }

    @Test
    void changeRole_asAdmin_returns200() throws Exception {
        mockMvc.perform(patch("/api/admin/users/" + targetUserId + "/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangeRoleRequest(User.Role.STAFF))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("STAFF")));
    }

    @Test
    void changeRole_asCustomer_returns403() throws Exception {
        mockMvc.perform(patch("/api/admin/users/" + targetUserId + "/role")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangeRoleRequest(User.Role.STAFF))))
                .andExpect(status().isForbidden());
    }

    @Test
    void toggleAccount_disablesUser() throws Exception {
        mockMvc.perform(patch("/api/admin/users/" + targetUserId + "/toggle")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountEnabled", is(false)));
    }

    @Test
    void toggleAccount_reenablesUser() throws Exception {
        mockMvc.perform(patch("/api/admin/users/" + targetUserId + "/toggle")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/admin/users/" + targetUserId + "/toggle")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountEnabled", is(true)));
    }

    @Test
    void getMe_returnsCurrentUser() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("customer@test.com")))
                .andExpect(jsonPath("$.role", is("CUSTOMER")));
    }
}
