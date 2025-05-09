package com.reboot.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
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
    // @Value("${jwt.expiration-ms}")
    // private long expirationMs;

    public SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // jwt Token 생성 (Access Token과 Refresh Token 분리를 위해 expiration 개별 설정)
    public String generateToken(String category, String username, String role) {

        Instant now = Instant.now();
        Date expiration = new Date(now.toEpochMilli() + SetExpirationMs(category));

        return Jwts.builder()
                .claim("category", category)
                .claim("username", username)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(expiration)
                .signWith(getSecretKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String getCategory(String token) {
        try {
            return parseClaims(token).get("category", String.class);
        } catch (JwtException e) {
            throw new BadCredentialsException("Invalid or expired JWT token", e);
        }
    }

    public String getUsername(String token) {
        try {
            return parseClaims(token).get("username", String.class);
        } catch (JwtException e) {
            throw new BadCredentialsException("Invalid or expired JWT token", e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        try {
            Object rolesObj = parseClaims(token).get("role");

            if (rolesObj instanceof String roleStr) {
                // 단일 문자열이면 리스트로 감싸서 반환
                return List.of(roleStr);
            }

            if (rolesObj instanceof List<?> rolesList) {
                return rolesList.stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .toList();
            }

            throw new BadCredentialsException("Invalid roles claim in JWT: unexpected type " + rolesObj.getClass());

        } catch (JwtException e) {
            throw new BadCredentialsException("Failed to extract roles from token", e);
        }
    }

    // jwt Token 검증
    public boolean validateToken(String token) {
        try {
            parseClaims(token); // throw 되지 않으면 유효
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warning("JWT validation failed: " + e.getMessage());
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        String username = getUsername(token);
        List<SimpleGrantedAuthority> authorities = getRoles(token).stream()
                .map(SimpleGrantedAuthority::new) // "ROLE_ADMIN" 그대로 사용
                .toList();

        UserDetails user = new User(username, "", authorities);
        return new UsernamePasswordAuthenticationToken(user, null, authorities);
    }

    
    // 공통 Claims Parser
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private long SetExpirationMs(String category) {
        long expirationMs = 0L;

        if(category.equals("access")) {
            expirationMs = 600000; // 10분
        }
        else if(category.equals("refresh")) {
            expirationMs = 86400000; // 1일
        }

        return expirationMs;
    }
}
