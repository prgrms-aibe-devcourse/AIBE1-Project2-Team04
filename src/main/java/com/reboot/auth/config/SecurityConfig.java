package com.reboot.auth.config;

import com.reboot.auth.jwt.CustomLogoutFilter;
import com.reboot.auth.jwt.JwtAuthenticationFilter;
import com.reboot.auth.jwt.JwtTokenProvider;
import com.reboot.auth.jwt.LoginFilter;
import com.reboot.auth.repository.RefreshTokenRepository;
import com.reboot.auth.service.RefreshTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration, JwtTokenProvider jwtTokenProvider, RefreshTokenService refreshTokenService, RefreshTokenRepository refreshTokenRepository) {

        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        // 경로별 인가 작업
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/","/auth/login", "/auth/sign", "/reissue").permitAll()
                        // .requestMatchers("/admin").hasRole("ADMIN")
                        .anyRequest().permitAll());

        http
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), LoginFilter.class)
                .addFilterBefore(new CustomLogoutFilter(jwtTokenProvider, refreshTokenRepository), LogoutFilter.class);

        http
                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtTokenProvider, refreshTokenService), UsernamePasswordAuthenticationFilter.class);

        http
                .sessionManagement((session) ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
