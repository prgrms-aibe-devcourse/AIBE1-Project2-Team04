package com.reboot.auth.controller;

import com.reboot.auth.jwt.JwtTokenProvider;
import com.reboot.auth.service.ReissueService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
public class ReissueController {


    private final ReissueService reissueService;
    private final JwtTokenProvider jwtTokenProvider;

    public ReissueController(ReissueService reissueService, JwtTokenProvider jwtTokenProvider) {
        this.reissueService = reissueService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        String token = extractRefreshTokenFromCookies(request);

        if (token == null) {
            return new ResponseEntity<>("refresh token null", HttpStatus.BAD_REQUEST);
        }

        try {
            String newAccess = reissueService.reissueAccessToken(token);
            String newRefresh = reissueService.reissueRefreshToken(token);

            response.setHeader(jwtTokenProvider.CATEGORY_ACCESS, newAccess);
            response.addCookie(createCookie(jwtTokenProvider.CATEGORY_REFRESH, newRefresh));
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

    private Cookie createCookie(String key, String vaule) {
        Cookie cookie = new Cookie(key, vaule);
        cookie.setMaxAge(24 * 60 * 60); // refreshToken과 동일하게
        cookie.setHttpOnly(true);
        return cookie;
    }
}
