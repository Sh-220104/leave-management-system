package com.epam.elms.unit.util;

import com.epam.elms.entity.Employee;
import com.epam.elms.service.impl.JwtUtil;
import com.epam.elms.utils.TestDataFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.*;

/**
 * Unit tests for {@link JwtUtil}.
 *
 * <p>No Spring context – plain POJO tests using reflection to inject @Value fields.
 */
public class JwtUtilTest {

    private static final String SECRET =
            "JH8hs@83hfKfW82jdls#9Uv4LjVtQm9pP1k3Ns8Bg2VtAe4GmQsVw4Zz6bHtWsTLP";
    private static final long EXPIRATION = 3_600_000L;

    private JwtUtil jwtUtil;

    @BeforeClass
    public void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", EXPIRATION);
    }

    // ── generateToken ─────────────────────────────────────────────────────────

    @Test(description = "Token should be generated and non-empty for a valid employee")
    public void generateToken_withValidEmployee_returnsNonEmptyToken() {
        Employee employee = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        String token = jwtUtil.generateToken(employee);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
    }

    @Test(description = "Different employees must produce different tokens")
    public void generateToken_differentEmployees_produceDifferentTokens() {
        Employee emp1 = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "h1");
        Employee emp2 = TestDataFactory.buildEmployee(2L, "Bob",   "bob@epam.com",   "h2");

        String token1 = jwtUtil.generateToken(emp1);
        String token2 = jwtUtil.generateToken(emp2);

        assertNotEquals(token1, token2, "Tokens for different users must differ");
    }

    // ── extractUsername ───────────────────────────────────────────────────────

    @Test(description = "extractUsername should return the employee email embedded as subject")
    public void extractUsername_validToken_returnsCorrectEmail() {
        Employee employee = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        String token = jwtUtil.generateToken(employee);

        String extracted = jwtUtil.extractUsername(token);

        assertEquals(extracted, "alice@epam.com",
                "Extracted username must match the employee email");
    }

    // ── validateToken ─────────────────────────────────────────────────────────

    @Test(description = "validateToken should return true for a valid token and matching UserDetails")
    public void validateToken_validTokenAndMatchingUser_returnsTrue() {
        Employee employee = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        String token = jwtUtil.generateToken(employee);

        UserDetails userDetails = User.withUsername("alice@epam.com")
                .password("hash")
                .authorities(Collections.emptyList())
                .build();

        boolean valid = jwtUtil.validateToken(token, userDetails);

        assertTrue(valid, "Token should be valid for the correct user");
    }

    @Test(description = "validateToken should return false when the username does not match")
    public void validateToken_wrongUser_returnsFalse() {
        Employee employee = TestDataFactory.buildEmployee(1L, "Alice", "alice@epam.com", "hash");
        String token = jwtUtil.generateToken(employee);

        UserDetails wrongUser = User.withUsername("other@epam.com")
                .password("hash")
                .authorities(Collections.emptyList())
                .build();

        boolean valid = jwtUtil.validateToken(token, wrongUser);

        assertFalse(valid, "Token should be invalid for a different user");
    }
}
