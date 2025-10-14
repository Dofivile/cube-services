package com.example.cube.service.supabass;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class TokenValidator {

    @Value("${supabase.jwt.secret}")
    private String jwtSecret;

    public boolean validateToken(String token) {
        try {
            // Parse token
            JWSObject jwsObject = JWSObject.parse(token);
            SignedJWT jwt = SignedJWT.parse(token);

            // 1. Verify signature
            MACVerifier verifier = new MACVerifier(jwtSecret);
            if (!jwsObject.verify(verifier)) {
                return false;
            }

            // 2. Check expiration
            Date expirationTime = jwt.getJWTClaimsSet().getExpirationTime();
            if (expirationTime == null || expirationTime.before(new Date())) {
                return false;  // Token expired
            }

            // 3. Check issued-at (optional but recommended)
            Date issuedAt = jwt.getJWTClaimsSet().getIssueTime();
            if (issuedAt != null && issuedAt.after(new Date())) {
                return false;  // Token issued in future
            }

            // 4. Check audience (verify it's for authenticated users)
            List<String> audience = jwt.getJWTClaimsSet().getAudience();
            if (audience == null || !audience.contains("authenticated")) {
                return false;  // Wrong audience
            }

            // All checks passed
            return true;

        } catch (Exception e) {
            return false;
        }
    }
}