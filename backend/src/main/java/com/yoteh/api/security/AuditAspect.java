package com.yoteh.api.security;

import com.yoteh.api.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditService auditService;

    public AuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {

        UUID userId = null;
        String userEmail = null;
        String ipAddress = null;
        String userAgent = null;
        String requestMethod = null;
        String requestPath = null;

        // ── Extraire les infos de la requête ──
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                ipAddress = getClientIp(request);
                userAgent = request.getHeader("User-Agent");
                requestMethod = request.getMethod();
                requestPath = request.getRequestURI();
            }
        } catch (Exception e) {
            log.debug("Impossible de récupérer la requête HTTP pour l'audit", e);
        }

        // ── Extraire les infos de l'utilisateur authentifié ──
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
                userId = userDetails.getId();
                userEmail = userDetails.getUsername();
            }
        } catch (Exception e) {
            log.debug("Impossible de récupérer l'utilisateur pour l'audit", e);
        }

        // ── Exécuter la méthode ──
        try {
            Object result = joinPoint.proceed();

            // ── Succès : log l'action ──
            String description =
                    auditable.description().isEmpty()
                            ? auditable.action() + " réussi"
                            : auditable.description();

            auditService.log(
                    userId,
                    userEmail,
                    auditable.action(),
                    auditable.entityType().isEmpty() ? null : auditable.entityType(),
                    null,
                    description,
                    ipAddress,
                    userAgent,
                    requestMethod,
                    requestPath,
                    200,
                    true);

            return result;

        } catch (Exception e) {
            // ── Échec : log l'erreur ──
            String description = auditable.action() + " échoué : " + e.getMessage();

            auditService.log(
                    userId,
                    userEmail,
                    auditable.action(),
                    auditable.entityType().isEmpty() ? null : auditable.entityType(),
                    null,
                    description,
                    ipAddress,
                    userAgent,
                    requestMethod,
                    requestPath,
                    null,
                    false);

            throw e;
        }
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
}
