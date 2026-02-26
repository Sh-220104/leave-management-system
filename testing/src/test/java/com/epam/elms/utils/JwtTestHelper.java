package com.epam.elms.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Generates JWT tokens for use in integration / API tests without
 * starting the full Spring security context.
 */
public final class JwtTestHelper {

    /** Must match jwt.secret in application-test.properties */
    private static final String SECRET =
            "JH8hs@83hfKfW82jdls#9Uv4LjVtQm9pP1k3Ns8Bg2VtAe4GmQsVw4Zz6bHtWsTLP";
    private static final long EXPIRATION_MS = 3_600_000L; // 1 hour

    private JwtTestHelper() {}

    /**
     * Generates a signed JWT token for the given email and roles.
     *
     * @param email the subject (user email)
     * @param roles set of role strings (e.g. {"ROLE_EMPLOYEE"})
     * @return compact JWT string
     */
    public static String generateToken(String email, Set<String> roles) {
        Key key = Keys.hmacShaKeyFor(SECRET.getBytes());
        return Jwts.builder()
                .setSubject(email)
                .addClaims(Map.of("roles", roles))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Convenience: employee-role token. */
    public static String employeeToken(String email) {
        return generateToken(email, Set.of("ROLE_EMPLOYEE"));
    }

    /** Convenience: admin-role token. */
    public static String adminToken(String email) {
        return generateToken(email, Set.of("ROLE_ADMIN"));
    }

    /** Returns "Bearer <token>" suitable for the Authorization header. */
    public static String bearerHeader(String token) {
        return "Bearer " + token;
    }
}
