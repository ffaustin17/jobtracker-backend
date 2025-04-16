package com.ffaustin.job_tracker.util;

import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

   private final SecretKey secretKey = Jwts.SIG.HS256.key().build();

   // Token validity duration (24 hours)
   private final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; //24 hours

    /**
     * Generates a JWT containing the user's email as the subject.
     * @param email the user's email.
     * @return JWT response
     */
    public String generateToken(String email){
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Extracts the email (subject) from the JWT
     * @param token
     * @return
     */
    public String extractEmail(String token){
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Validates the JWT signature and structure.
     * @param token
     * @return
     */
    public boolean isTokenValid(String token){
        try{
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);

            return true;
        }
        catch(Exception e){
            return false;
        }
    }
}
