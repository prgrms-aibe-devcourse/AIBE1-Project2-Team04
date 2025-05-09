package com.reboot.auth.service;

import com.reboot.auth.entity.RefreshToken;
import com.reboot.auth.jwt.JwtTokenProvider;
import com.reboot.auth.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtTokenProvider jwtTokenProvider) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public void addRefreshEntity(String username, String token){
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUsername(username);
        refreshToken.setToken(token);
        refreshToken.setExpiration(getExpiration());

        refreshTokenRepository.save(refreshToken);
    }

    public void deleteRefreshToken(String token){
        refreshTokenRepository.deleteByToken(token);
    }

    private String getExpiration(){
        Instant now = Instant.now();
        Date expiration = new Date(now.toEpochMilli() + jwtTokenProvider.GetExpirationMs(jwtTokenProvider.CATEGORY_REFRESH));
        return expiration.toString();
    }
}