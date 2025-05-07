package com.reboot.auth.jwt;

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

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String accessToken = req.getHeader(jwtTokenProvider.CATEGORY_ACCESS);

        if (accessToken == null) {
            chain.doFilter(req, res);
            return;
        }

        // 만료 여부 확인
        try {
            jwtTokenProvider.validateToken(accessToken);
        } catch (ExpiredJwtException e) {
            PrintWriter writer = res.getWriter();
            writer.println("Expired Access Token");

            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
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
}
