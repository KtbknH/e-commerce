package com.yoteh.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Endpoints publics (pas besoin d'authentification)
    private static final String[] PUBLIC_URLS = {
        "/auth/**",
        "/products/**",
        "/categories/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**",
        "/actuator/**",
        "/health"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Désactiver CSRF (API REST stateless)
                .csrf(csrf -> csrf.disable())

                // Pas de sessions (JWT sera ajouté au Chat 3)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Règles d'autorisation
                .authorizeHttpRequests(
                        auth ->
                                auth
                                        // Endpoints publics
                                        .requestMatchers(PUBLIC_URLS)
                                        .permitAll()
                                        // Tout le reste nécessite une authentification (Chat 3)
                                        .anyRequest()
                                        .permitAll() // TODO: changer en .authenticated() au Chat 3
                        );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
