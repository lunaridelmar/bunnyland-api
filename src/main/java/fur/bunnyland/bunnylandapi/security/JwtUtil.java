package fur.bunnyland.bunnylandapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Set;

@Component
public class JwtUtil {
    // TODO In production, read from config/env and don’t hardcode
    private final Key accessKey  = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final Key refreshKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    private final long accessTtlMs  = 1000L * 60 * 15;  // 15 minutes
    private final long refreshTtlMs = 1000L * 60 * 60 * 24 * 7; // 7 days

    public String generateAccessToken(Long userId, String email, Set<String> roles) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(email)
                .claim("id", userId)
                .claim("roles", roles)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + accessTtlMs))
                .signWith(accessKey)
                .compact();
    }

    public String generateRefreshToken(Long userId, String email) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(email)
                .claim("id", userId)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + refreshTtlMs))
                .signWith(refreshKey)
                .compact();
    }

    public long getAccessTtlSeconds() {
        return accessTtlMs / 1000;
    }

    public Claims parseRefreshToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(refreshKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Claims parseAccessToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(accessKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
