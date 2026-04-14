package com.yoteh.api.service;

import com.yoteh.api.dto.response.AuditLogResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import java.time.LocalDateTime;
import java.util.UUID;

public interface AuditService {

    /** Enregistrer une action d'audit. */
    void log(
            UUID userId,
            String userEmail,
            String action,
            String entityType,
            UUID entityId,
            String description,
            String ipAddress,
            String userAgent,
            String requestMethod,
            String requestPath,
            Integer statusCode,
            boolean success);

    /** Raccourci : log une action réussie. */
    void logSuccess(
            UUID userId,
            String userEmail,
            String action,
            String entityType,
            UUID entityId,
            String description,
            String ipAddress);

    /** Raccourci : log un échec (tentative de connexion, action refusée). */
    void logFailure(String userEmail, String action, String description, String ipAddress);

    /** Lister les logs d'audit avec filtres (admin). */
    PagedResponse<AuditLogResponse> getAuditLogs(
            UUID userId,
            String action,
            String entityType,
            Boolean success,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            int size);
}
