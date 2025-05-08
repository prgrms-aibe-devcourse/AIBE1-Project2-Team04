package com.reboot.auth.jwt;

import com.reboot.auth.service.ReissueService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final ReissueService reissueService;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String accessToken = jwtTokenProvider.getTokenFromCookies(jwtTokenProvider.CATEGORY_ACCESS, req);
        String refreshToken = jwtTokenProvider.getTokenFromCookies(jwtTokenProvider.CATEGORY_REFRESH, req);

        if (isStringEmpty(accessToken) || isStringEmpty(refreshToken)) {
            chain.doFilter(req, res);
            return;
        }

        // 만료 여부 확인
        if (!jwtTokenProvider.validateToken(accessToken)){
            accessToken = reissueService.reissueAccessToken(refreshToken);

            if(isStringEmpty(accessToken)){
                PrintWriter writer = res.getWriter();
                writer.println("Invalid Refresh Token");

                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            res.addCookie(jwtTokenProvider.createCookie(jwtTokenProvider.CATEGORY_ACCESS, accessToken));
        }

        String category = jwtTokenProvider.getCategory(accessToken);
        if (!category.equals(jwtTokenProvider.CATEGORY_ACCESS)) {
            PrintWriter writer = res.getWriter();
            writer.println("Invalid Access Token");

            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Authentication auth = jwtTokenProvider.getAuthentication(accessToken);
        SecurityContextHolder.getContext().setAuthentication(auth);

        chain.doFilter(req, res);
    }

    private boolean isStringEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
