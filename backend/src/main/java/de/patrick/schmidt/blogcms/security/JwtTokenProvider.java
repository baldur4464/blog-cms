package de.patrick.schmidt.blogcms.security;

import de.patrick.schmidt.blogcms.config.SecurityProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final SecurityProperties securityProperties;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(
                securityProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UUID userId, String username, String role, String preferredLocale) {
        long ttlMs = securityProperties.getJwt().getAccessTokenTtl().toMillis();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .claim("roles", List.of(role))
                .claim("type", "access")
                .claim("preferredLocale", preferredLocale) // null ⇒ Claim wird weggelassen
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ttlMs))
                .signWith(key())
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isAccessToken(String token) {
        try {
            Claims claims = parseToken(token);
            return "access".equals(claims.get("type", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public UUID getUserIdFromToken(String token) {
        return UUID.fromString(parseToken(token).getSubject());
    }
}
