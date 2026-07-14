package com.example.ppm.security;

import org.junit.jupiter.api.Test;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class JwtUtilTest {
    private final JwtUtil jwtUtil = new JwtUtil("abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz", 3600);

    @Test
    void createsAndParsesAuthenticatedUser() {
        String token = jwtUtil.createToken(12L, "admin", "ADMIN");

        JwtUser user = jwtUtil.parseToken(token);

        assertEquals(12L, user.userId());
        assertEquals("admin", user.username());
        assertEquals("ADMIN", user.role());
    }

    @Test
    void rejectsMalformedToken() {
        assertFalse(jwtUtil.isValid("not-a-token"));
    }

    @Test
    void allowsCorsPreflightWithoutAuthentication() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("OPTIONS");

        assertTrue(new JwtInterceptor(jwtUtil).preHandle(request, mock(HttpServletResponse.class), new Object()));
    }
}
