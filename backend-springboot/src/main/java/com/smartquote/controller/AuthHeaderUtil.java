package com.smartquote.controller;

import org.springframework.http.HttpHeaders;

public final class AuthHeaderUtil {

    private AuthHeaderUtil() {}

    public static String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }
        if (authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7).trim();
        }
        return authorizationHeader.trim();
    }

    public static String extractToken(HttpHeaders headers) {
        return extractToken(headers.getFirst(HttpHeaders.AUTHORIZATION));
    }
}
