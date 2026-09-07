package com.datacraft.auth.security;

import com.datacraft.auth.domain.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

public class JwtTokenService {
    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtTokenService(JwtProperties properties) {
        this.properties = properties;
        try {
            this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("JWT secret must be a base64-encoded key of at least 256 bits", exception);
        }
    }

    public String issue(UserAccount account) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(account.username())
                .claim("uid", account.id())
                .claim("roles", account.roleCodes())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(properties.expirationSeconds())))
                .signWith(signingKey)
                .compact();
    }

    public DataCraftPrincipal parse(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
            Number userId = claims.get("uid", Number.class);
            List<?> rawRoles = claims.get("roles", List.class);
            List<String> roles = rawRoles == null ? List.of() : rawRoles.stream().map(String::valueOf).toList();
            return new DataCraftPrincipal(userId == null ? null : userId.longValue(), claims.getSubject(), roles);
        } catch (RuntimeException exception) {
            throw new InvalidTokenException(exception);
        }
    }

    public long expirationSeconds() {
        return properties.expirationSeconds();
    }
}
