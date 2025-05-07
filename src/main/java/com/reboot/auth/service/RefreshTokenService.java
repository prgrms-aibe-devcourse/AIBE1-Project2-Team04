package com.reboot.auth.service;

import com.reboot.auth.entity.RefreshToken;
import com.reboot.auth.jwt.JwtTokenProvider;
import com.reboot.auth.repository.RefreshTokenRepository;
import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtTokenProvider jwtTokenProvider) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public void addRefreshEntity(String username, String token, String expiration){
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUsername(username);
        refreshToken.setToken(token);
        refreshToken.setExpiration(expiration);

        refreshTokenRepository.save(refreshToken);
    }

    public void deleteRefreshToken(String token){
        refreshTokenRepository.deleteByToken(token);
    }

    public Cookie createCookie(String key, String vaule) {
        Cookie cookie = new Cookie(key, vaule);
        cookie.setMaxAge((int)jwtTokenProvider.GetExpirationMs(jwtTokenProvider.CATEGORY_REFRESH)); // refreshToken과 동일하게
        cookie.setHttpOnly(true);
        return cookie;
    }
}
