package com.reboot.auth.oauth2;

import com.reboot.auth.jwt.JwtTokenProvider;
import com.reboot.auth.service.MemberService;
import com.reboot.auth.service.RefreshTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collection;

public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final MemberService memberService;

    public OAuth2LoginSuccessHandler(JwtTokenProvider jwtTokenProvider, RefreshTokenService refreshTokenService, MemberService memberService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.memberService = memberService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        boolean isRegistered = memberService.existsByEmail(email);
        if(!isRegistered) {
            redirectStrategy.sendRedirect(request, response, "/auth/sign?type=user&email=" + URLEncoder.encode(email, "UTF-8") + "&name=" + URLEncoder.encode(name, "UTF-8"));
            return;
        }

        String username = memberService.getUsernameByEmail(email);

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        GrantedAuthority grantedAuthority = authorities.iterator().next();
        String role = grantedAuthority.getAuthority();

        String accessToken = jwtTokenProvider.generateToken(jwtTokenProvider.CATEGORY_ACCESS, username, role);
        String refreshToken = jwtTokenProvider.generateToken(jwtTokenProvider.CATEGORY_REFRESH, username, role);

        // Refresh Token DB 저장
        refreshTokenService.addRefreshEntity(username, refreshToken);

        response.addCookie(jwtTokenProvider.createCookie(jwtTokenProvider.CATEGORY_ACCESS, accessToken));
        response.addCookie(jwtTokenProvider.createCookie(jwtTokenProvider.CATEGORY_REFRESH, refreshToken));
        response.sendRedirect("/");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
