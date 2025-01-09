package dev.vudovenko.eventmanagement.security.jwt;

import dev.vudovenko.eventmanagement.users.userRoles.UserRole;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenManager {

    private final SecretKey key;
    private final long expirationTime;

    public JwtTokenManager(
            @Value("${jwt.secret-key}") String keyString,
            @Value("${jwt.lifetime}") long expirationTime
    ) {
        this.key = Keys.hmacShaKeyFor(keyString.getBytes());
        this.expirationTime = expirationTime;
    }

    public String generateToken(String login, UserRole role) {
        return Jwts
                .builder()
                .subject(login)
                .signWith(key)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .claim("role", role.name())
                .compact();
    }

    public String generateToken(String login, Long userId, UserRole role) {
        return Jwts
                .builder()
                .subject(login)
                .signWith(key)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .claim("userId", userId)
                .claim("role", role.name())
                .compact();
    }

    public String getLoginFromToken(String jwt) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(jwt)
                .getPayload()
                .getSubject();
    }

    public boolean isTokenValid(String jwt) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(jwt);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
