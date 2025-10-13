package com.example.cube.service.supabass;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.MACVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TokenValidator {

    @Value("${supabase.jwt.secret}")
    private String jwtSecret;

    public boolean validateToken(String token) {
        try {
            JWSObject jwsObject = JWSObject.parse(token);
            MACVerifier verifier = new MACVerifier(jwtSecret);
            boolean valid = jwsObject.verify(verifier);
            return valid;
        } catch (Exception e) {
            return false;
        }
    }
    // todo this doesn't verfiy who the jwt token belomgs to
}
