package com.reboot.auth.service;

import com.reboot.auth.jwt.JwtTokenProvider;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.stereotype.Service;

@Service
public class ReissueService {

    private final JwtTokenProvider jwtTokenProvider;


    public ReissueService(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String reissueToken(String token) {
        try {
            jwtTokenProvider.validateToken(token);
        } catch (ExpiredJwtException e) {
            throw new IllegalArgumentException("refresh token expired");
        }


        String category = jwtTokenProvider.getCategory(token);
        if (!"refresh".equals(category)) {
            throw new IllegalArgumentException("invalid refresh token");
        }

        String username = jwtTokenProvider.getUsername(token);
        String role = jwtTokenProvider.getRole(token);

        // 새 access 토큰 발급
        return jwtTokenProvider.generateToken("access", username, role);
    }
}
