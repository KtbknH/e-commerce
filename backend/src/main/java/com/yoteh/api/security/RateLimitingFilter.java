package com.yoteh.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoteh.api.dto.response.common.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private final ObjectMapper objectMapper;

    @Value("${app.security.rate-limit.max-requests:60}")
    private int maxRequests;

    @Value("${app.security.rate-limit.window-seconds:60}")
    private int windowSeconds;

    @Value("${app.security.rate-limit.auth-max-requests:10}")
    private int authMaxRequests;

    @Value("${app.security.rate-limit.auth-window-seconds:300}")
    private int authWindowSeconds;

    private final Map<String, ClientRateInfo> clients = new ConcurrentHashMap<>();

    public RateLimitingFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = getClientIp(request);
        String path = request.getRequestURI();

        boolean isAuthEndpoint =
                path.startsWith("/api/v1/auth/login")
                        || path.startsWith("/api/v1/auth/register")
                        || path.startsWith("/api/v1/auth/forgot-password");

        int limit = isAuthEndpoint ? authMaxRequests : maxRequests;
        int window = isAuthEndpoint ? authWindowSeconds : windowSeconds;

        String key = isAuthEndpoint ? "auth:" + clientIp : "api:" + clientIp;

        ClientRateInfo rateInfo = clients.computeIfAbsent(key, k -> new ClientRateInfo());

        long now = System.currentTimeMillis();
        long windowMillis = window * 1000L;

        if (now - rateInfo.getWindowStart() > windowMillis) {
            rateInfo.reset(now);
        }

        int currentCount = rateInfo.incrementAndGet();

        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader(
                "X-RateLimit-Remaining", String.valueOf(Math.max(0, limit - currentCount)));
        response.setHeader(
                "X-RateLimit-Reset",
                String.valueOf((rateInfo.getWindowStart() + windowMillis) / 1000));

        if (currentCount > limit) {
            log.warn(
                    "Rate limit dépassé pour IP={} sur path={} ({}/{})",
                    clientIp,
                    path,
                    currentCount,
                    limit);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ErrorResponse error =
                    new ErrorResponse(
                            HttpStatus.TOO_MANY_REQUESTS.value(),
                            "TOO_MANY_REQUESTS",
                            "Trop de requêtes. Réessayez dans quelques instants.",
                            path,
                            LocalDateTime.now(),
                            Collections.emptyList());

            objectMapper.writeValue(response.getOutputStream(), error);
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/health")
                || path.startsWith("/actuator");
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    /** Nettoyage périodique des entrées expirées (appelé par scheduled task). */
    public void cleanupExpiredEntries() {
        long now = System.currentTimeMillis();
        long maxWindow = Math.max(windowSeconds, authWindowSeconds) * 1000L;
        clients.entrySet()
                .removeIf(entry -> now - entry.getValue().getWindowStart() > maxWindow * 2);
    }

    // ── Inner class ──

    private static class ClientRateInfo {
        private final AtomicLong windowStart = new AtomicLong(System.currentTimeMillis());
        private final AtomicInteger count = new AtomicInteger(0);

        public long getWindowStart() {
            return windowStart.get();
        }

        public int incrementAndGet() {
            return count.incrementAndGet();
        }

        public void reset(long newStart) {
            windowStart.set(newStart);
            count.set(0);
        }
    }
}
