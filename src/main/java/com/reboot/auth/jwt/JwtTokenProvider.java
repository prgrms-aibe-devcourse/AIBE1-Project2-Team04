package com.reboot.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
@Log
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;
    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    public SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // jwt Token 생성
    public String generateToken(Authentication authentication, List<String> roles) {
        String username = authentication.getName();
        Instant now = Instant.now(); // UTC.
        Date expiration = new Date(now.toEpochMilli() + expirationMs);
        log.info("roles : %s".formatted(roles));
        Claims claims = Jwts.claims()
                .subject(username)
                .add("roles", roles)
                .build();
        return Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(now)) // 토큰생성일자
                .expiration(expiration) // 만료일자
                .claims(claims)
                .signWith(getSecretKey(), Jwts.SIG.HS256) // 변환 알고리즘
                .compact();
    }

    public String getUsername(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        return (List<String>) Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("roles");
    }

    // jwt Token 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        UserDetails user = new User(getUsername(token), "",
                getRoles(token).stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_%s".formatted(role))) // ROLE 붙여야 된다... 자동완성 믿지마!!!
                        .toList());
        // 문자열 -> 권한 클래스 객체
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }
}
