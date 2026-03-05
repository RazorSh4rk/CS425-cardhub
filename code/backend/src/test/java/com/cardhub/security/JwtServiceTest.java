package com.cardhub.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private static final String SECRET = "testSecretKeyForUnitTestingOnlyMustBeAtLeast32Bytes";
    private static final long EXPIRATION = 86400000;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION);
    }

    @Test
    void generateToken_createsValidToken() {
        String token = jwtService.generateToken(1L, "test@example.com", "CUSTOMER");

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void validateToken_withValidToken_returnsTrue() {
        String token = jwtService.generateToken(1L, "test@example.com", "CUSTOMER");

        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void validateToken_withInvalidToken_returnsFalse() {
        assertFalse(jwtService.validateToken("invalid.token.here"));
    }

    @Test
    void validateToken_withNullToken_returnsFalse() {
        assertFalse(jwtService.validateToken(null));
    }

    @Test
    void validateToken_withEmptyToken_returnsFalse() {
        assertFalse(jwtService.validateToken(""));
    }

    @Test
    void validateToken_withTamperedToken_returnsFalse() {
        String token = jwtService.generateToken(1L, "test@example.com", "CUSTOMER");
        String tamperedToken = token.substring(0, token.length() - 5) + "xxxxx";

        assertFalse(jwtService.validateToken(tamperedToken));
    }

    @Test
    void getUserIdFromToken_returnsCorrectId() {
        String token = jwtService.generateToken(42L, "test@example.com", "CUSTOMER");

        assertEquals(42L, jwtService.getUserIdFromToken(token));
    }

    @Test
    void getEmailFromToken_returnsCorrectEmail() {
        String token = jwtService.generateToken(1L, "test@example.com", "CUSTOMER");

        assertEquals("test@example.com", jwtService.getEmailFromToken(token));
    }

    @Test
    void getRoleFromToken_returnsCorrectRole() {
        String token = jwtService.generateToken(1L, "test@example.com", "ADMIN");

        assertEquals("ADMIN", jwtService.getRoleFromToken(token));
    }

    @Test
    void generateToken_withDifferentRoles_createsDistinctTokens() {
        String customerToken = jwtService.generateToken(1L, "test@example.com", "CUSTOMER");
        String adminToken = jwtService.generateToken(1L, "test@example.com", "ADMIN");

        assertNotEquals(customerToken, adminToken);
        assertEquals("CUSTOMER", jwtService.getRoleFromToken(customerToken));
        assertEquals("ADMIN", jwtService.getRoleFromToken(adminToken));
    }

    @Test
    void validateToken_withExpiredToken_returnsFalse() {
        JwtService shortLivedService = new JwtService(SECRET, 1);
        String token = shortLivedService.generateToken(1L, "test@example.com", "CUSTOMER");

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertFalse(shortLivedService.validateToken(token));
    }
}
