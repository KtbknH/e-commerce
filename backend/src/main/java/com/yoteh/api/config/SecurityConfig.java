package com.yoteh.api.config;

import com.yoteh.api.security.JwtAuthEntryPoint;
import com.yoteh.api.security.JwtAuthFilter;
import com.yoteh.api.security.RateLimitingFilter;
import com.yoteh.api.security.XssSanitizationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final RateLimitingFilter rateLimitingFilter;
    private final XssSanitizationFilter xssSanitizationFilter;
    private final SecurityHeadersConfig securityHeadersConfig;

    public SecurityConfig(
            JwtAuthFilter jwtAuthFilter,
            JwtAuthEntryPoint jwtAuthEntryPoint,
            RateLimitingFilter rateLimitingFilter,
            XssSanitizationFilter xssSanitizationFilter,
            SecurityHeadersConfig securityHeadersConfig) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
        this.rateLimitingFilter = rateLimitingFilter;
        this.xssSanitizationFilter = xssSanitizationFilter;
        this.securityHeadersConfig = securityHeadersConfig;
    }

    // Endpoints publics (pas besoin d'authentification)
    private static final String[] PUBLIC_URLS = {
        "/api/v1/auth/register",
        "/api/v1/auth/login",
        "/api/v1/auth/refresh-token",
        "/api/v1/auth/forgot-password",
        "/api/v1/auth/reset-password",
        "/api/v1/auth/verify-email",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**",
        "/actuator/**",
        "/health"
    };

    // Endpoints GET publics (consultation sans authentification)
    private static final String[] PUBLIC_GET_URLS = {
        "/api/v1/products/**",
        "/api/v1/categories/**",
        "/api/v1/shipping/zones",
        "/api/v1/shipping/zones/**",
        "/api/v1/promotions/flash-sales"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // ─── CSRF désactivé (API REST stateless avec JWT) ───
        http.csrf(csrf -> csrf.disable());

        // ─── CORS ───
        http.cors(cors -> {});

        // ─── Sessions stateless ───
        http.sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // ─── Exception handling ───
        http.exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthEntryPoint));

        // ─── Autorisation des requêtes ───
        http.authorizeHttpRequests(
                auth ->
                        auth.requestMatchers(PUBLIC_URLS)
                                .permitAll()
                                .requestMatchers(HttpMethod.GET, PUBLIC_GET_URLS)
                                .permitAll()
                                .requestMatchers("/api/v1/admin/**")
                                .hasRole("ADMIN")
                                .anyRequest()
                                .authenticated());

        // ─── Chaîne de filtres ───
        // Ordre : XSS → RateLimit → JWT
        http.addFilterBefore(xssSanitizationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(rateLimitingFilter, XssSanitizationFilter.class);
        http.addFilterBefore(jwtAuthFilter, RateLimitingFilter.class);

        // ─── Security Headers (HSTS, CSP, X-Frame-Options, etc.) ───
        securityHeadersConfig.configure(http);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
