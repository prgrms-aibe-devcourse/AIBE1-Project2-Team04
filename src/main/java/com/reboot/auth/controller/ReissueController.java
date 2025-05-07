package com.reboot.auth.controller;

import com.reboot.auth.jwt.JwtTokenProvider;
import com.reboot.auth.service.RefreshTokenService;
import com.reboot.auth.service.ReissueService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Instant;
import java.util.Date;

@Controller
@ResponseBody
public class ReissueController {


    private final JwtTokenProvider jwtTokenProvider;
    private final ReissueService reissueService;
    private final RefreshTokenService refreshTokenService;

    public ReissueController(ReissueService reissueService, JwtTokenProvider jwtTokenProvider, RefreshTokenService refreshTokenService) {
        this.reissueService = reissueService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        String token = extractRefreshTokenFromCookies(request);

        if (token == null) {
            return new ResponseEntity<>("refresh token null", HttpStatus.BAD_REQUEST);
        }

        try {
            String username = jwtTokenProvider.getUsername(token);
            String newAccess = reissueService.reissueAccessToken(token);
            String newRefresh = reissueService.reissueRefreshToken(token);

            // 기존 Refresh Token 삭제 후 New Refresh Token DB 저장
            Instant now = Instant.now();
            Date expiration = new Date(now.toEpochMilli() + jwtTokenProvider.GetExpirationMs(jwtTokenProvider.CATEGORY_REFRESH));
            String refresh_Expiration = expiration.toString();

            refreshTokenService.deleteRefreshToken(token);
            refreshTokenService.addRefreshEntity(username, newRefresh, refresh_Expiration);

            response.setHeader(jwtTokenProvider.CATEGORY_ACCESS, newAccess);
            response.addCookie(refreshTokenService.createCookie(jwtTokenProvider.CATEGORY_REFRESH, newRefresh));
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // refresh token 추출
    private String extractRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if (jwtTokenProvider.CATEGORY_REFRESH.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
