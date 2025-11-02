package com.chethan.PasswordMailOTP.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Component responsible for JWT token generation, validation, and parsing.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String INVALID_TOKEN_MSG = "Invalid JWT token: {}";
    
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration:86400000}") // Default 24 hours
    private long jwtExpirationInMs;

    /**
     * Generates a JWT token for the given email.
     *
     * @param email the user's email
     * @return the generated JWT token
     */
    public String generateToken(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        return createToken(new HashMap<>(), email);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + jwtExpirationInMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    private Key getSigningKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
            if (keyBytes.length < 32) { // Minimum key length for HS512
                throw new IllegalStateException("JWT secret key is too short. It must be at least 256 bits (32 characters)");
            }
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
            log.error("Failed to decode JWT secret: {}", e.getMessage());
            throw new JwtException("Invalid JWT configuration");
        }
    }

    /**
     * Validates the JWT token against the provided user details.
     *
     * @param token the JWT token to validate
     * @param userDetails the user details to validate against
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        if (token == null || userDetails == null) {
            return false;
        }
        
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            log.debug(INVALID_TOKEN_MSG, e.getMessage());
            return false;
        }
    }

    /**
     * Extracts the username from the JWT token.
     *
     * @param token the JWT token
     * @return the username (subject) from the token
     * @throws JwtException if the token is invalid or expired
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from the JWT token.
     *
     * @param token the JWT token
     * @return the expiration date
     * @throws JwtException if the token is invalid or expired
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from the JWT token.
     *
     * @param <T> the type of the claim
     * @param token the JWT token
     * @param claimsResolver function to resolve the claim
     * @return the claim value
     * @throws JwtException if the token is invalid or expired
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException ex) {
            log.debug(INVALID_TOKEN_MSG, "Token expired");
            throw new JwtException("Token has expired");
        } catch (UnsupportedJwtException ex) {
            log.debug(INVALID_TOKEN_MSG, "Unsupported JWT token");
            throw new JwtException("Unsupported JWT token");
        } catch (MalformedJwtException ex) {
            log.debug(INVALID_TOKEN_MSG, "Invalid JWT token");
            throw new JwtException("Invalid JWT token");
        } catch (SignatureException ex) {
            log.debug(INVALID_TOKEN_MSG, "Invalid JWT signature");
            throw new JwtException("Invalid JWT signature");
        } catch (IllegalArgumentException ex) {
            log.debug(INVALID_TOKEN_MSG, "JWT claims string is empty");
            throw new JwtException("JWT claims string is empty");
        }
    }

    private boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (JwtException e) {
            return true; // If we can't extract expiration, consider it expired
        }
    }
}
