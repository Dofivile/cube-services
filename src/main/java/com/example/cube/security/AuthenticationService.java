package com.example.cube.security;

import com.example.cube.exception.UnauthorizedException;
import com.example.cube.repository.UserDetailsRepository;
import com.example.cube.service.UserDetailsSyncService;
import com.example.cube.service.supabass.TokenValidator;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthenticationService {

    private final TokenValidator tokenValidator;
    private final UserDetailsRepository userRepository;
    private final UserDetailsSyncService userDetailsSyncService;

    @Autowired
    public AuthenticationService(TokenValidator tokenValidator,
                                 UserDetailsRepository userRepository,
                                 UserDetailsSyncService userDetailsSyncService) {
        this.tokenValidator = tokenValidator;
        this.userRepository = userRepository;
        this.userDetailsSyncService = userDetailsSyncService;
    }

    public UUID validateAndExtractUserId(String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        if (!tokenValidator.validateToken(token)) {
            throw new UnauthorizedException("Invalid or expired token");
        }

        UUID userId = extractUserIdFromToken(token);

        // Ensure we have a local user_details record (handles Google/Apple sign-ins).
        userDetailsSyncService.ensureUserDetails(userId);

        // Verify user actually exists in db
        if (!userRepository.existsById(userId)) {
            throw new UnauthorizedException("User not found");
        }

        return userId;
    }

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
