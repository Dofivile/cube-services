package com.example.cube.security;

import com.example.cube.exception.UnauthorizedException;
import com.example.cube.service.supabass.TokenValidator;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthenticationService {

    private final TokenValidator tokenValidator;

    @Autowired
    public AuthenticationService(TokenValidator tokenValidator) {
        this.tokenValidator = tokenValidator;
    }

    /**
     * Validates Authorization header and extracts user ID from JWT token
     * @param authHeader The Authorization header value (should start with "Bearer ")
     * @return The user ID extracted from the token
     * @throws UnauthorizedException if token is missing, invalid, or expired
     */
    public UUID validateAndExtractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix

        if (!tokenValidator.validateToken(token)) {
            throw new UnauthorizedException("Invalid or expired token");
        }

        return extractUserIdFromToken(token);
    }

    /**
     * Extracts user ID from JWT token
     * @param token The JWT token string
     * @return The user ID (subject) from the token
     * @throws UnauthorizedException if token cannot be parsed
     */
    private UUID extractUserIdFromToken(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            String userId = jwt.getJWTClaimsSet().getSubject();
            return UUID.fromString(userId);
        } catch (Exception e) {
            throw new UnauthorizedException("Failed to extract user ID from token: " + e.getMessage());
        }
    }
}