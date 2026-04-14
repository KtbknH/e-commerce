package com.yoteh.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

/**
 * Configuration des Security Headers HTTP.
 *
 * <p>Ce bean personnalise les headers Spring Security via {@code HttpSecurity.headers()}. Il est
 * injecté dans {@link SecurityConfig} via un {@code SecurityHeadersCustomizer}.
 */
@Configuration
public class SecurityHeadersConfig {

    /**
     * Applique les headers de sécurité sur la chaîne HttpSecurity. Appelé depuis SecurityConfig.
     */
    public void configure(HttpSecurity http) throws Exception {
        http.headers(
                headers -> {
                    // ─── HSTS (HTTP Strict Transport Security) ───
                    headers.httpStrictTransportSecurity(
                            hsts ->
                                    hsts.includeSubDomains(true)
                                            .maxAgeInSeconds(31536000)
                                            .preload(true));

                    // ─── X-Content-Type-Options: nosniff ───
                    headers.contentTypeOptions(cto -> {});

                    // ─── X-Frame-Options: DENY ───
                    headers.frameOptions(fo -> fo.deny());

                    // ─── X-XSS-Protection ───
                    headers.xssProtection(
                            xss ->
                                    xss.headerValue(
                                            XXssProtectionHeaderWriter.HeaderValue
                                                    .ENABLED_MODE_BLOCK));

                    // ─── Referrer-Policy ───
                    headers.referrerPolicy(
                            rp ->
                                    rp.policy(
                                            ReferrerPolicyHeaderWriter.ReferrerPolicy
                                                    .STRICT_ORIGIN_WHEN_CROSS_ORIGIN));

                    // ─── Permissions-Policy ───
                    headers.permissionsPolicy(
                            pp ->
                                    pp.policy(
                                            "camera=(), microphone=(), geolocation=(), "
                                                    + "payment=(), usb=(), magnetometer=(), gyroscope=()"));

                    // ─── Content-Security-Policy ───
                    headers.contentSecurityPolicy(
                            csp ->
                                    csp.policyDirectives(
                                            "default-src 'self'; "
                                                    + "script-src 'self'; "
                                                    + "style-src 'self' 'unsafe-inline'; "
                                                    + "img-src 'self' data: https:; "
                                                    + "font-src 'self'; "
                                                    + "connect-src 'self'; "
                                                    + "frame-ancestors 'none'; "
                                                    + "base-uri 'self'; "
                                                    + "form-action 'self'"));
                });
    }
}
