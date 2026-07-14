package com.example.ppm.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {
    private final SecretKey key;
    private final long expireSeconds;

    public JwtUtil(String secret, long expireSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireSeconds = expireSeconds;
    }

    public String createToken(Long userId, String username, String role) {
        Date now = new Date();
        return Jwts.builder().subject(String.valueOf(userId)).claim("username", username).claim("role", role)
                .issuedAt(now).expiration(new Date(now.getTime() + expireSeconds * 1000)).signWith(key).compact();
    }

    public JwtUser parseToken(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return new JwtUser(Long.valueOf(claims.getSubject()), claims.get("username", String.class), claims.get("role", String.class));
    }

    public boolean isValid(String token) {
        try { parseToken(token); return true; } catch (Exception ignored) { return false; }
    }
}
