package com.shanshan.auction.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
public class JwtUtils {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private Key getSigningKey() {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(secret);
            log.debug("Decoded key length: {} bytes", keyBytes.length);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            log.error("Failed to decode secret key", e);
            throw e;
        }
    }

    public String generateToken(Long userId) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + expiration);
            log.debug("Generating token for user: {}, expiry: {}", userId, expiryDate);

            return Jwts.builder()
                    .setSubject(userId.toString())
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            log.error("Failed to generate token for user: {}", userId, e);
            throw e;
        }
    }

    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            log.debug("Extracted user ID from token: {}", claims.getSubject());
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            log.error("Failed to extract user ID from token", e);
            throw e;
        }
    }

    public boolean validateToken(String token) {
        log.info("userId{}", getUserIdFromToken(token));
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            log.debug("Token validated successfully");
            return true;
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}