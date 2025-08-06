package org.innowise.internship.authenticationservice.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private final long EXPIRATION_TIME = 1000 * 60 * 15; // 15 минут
    private final long REFRESH_EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 7; // 7 дней

    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        // decode base64 string и получить Key для HMAC
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION_TIME))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }


    public Jws<Claims> validateToken(String token) {

        Jws<Claims> jws = parseToken(token);

        String subject = jws.getPayload().getSubject();
        if (subject == null || subject.isEmpty()) {
            throw new IllegalArgumentException("Subject (sub) is empty in the token");
        }

        return jws;

    }
    public Jws<Claims> parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
    }

    public Long getUserIdFromToken(String token) {
        return Long.parseLong(validateToken(token).getPayload()
                .getSubject()
        );
    }


}