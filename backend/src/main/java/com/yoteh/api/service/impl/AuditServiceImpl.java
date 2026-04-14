package com.yoteh.api.service.impl;

import com.yoteh.api.dto.response.AuditLogResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import com.yoteh.api.entity.AuditLog;
import com.yoteh.api.repository.AuditLogRepository;
import com.yoteh.api.service.AuditService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditServiceImpl implements AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditServiceImpl.class);

    private final AuditLogRepository auditLogRepository;

    public AuditServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(
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
            boolean success) {

        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(userId);
            auditLog.setUserEmail(userEmail);
            auditLog.setAction(action);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setDescription(description);
            auditLog.setIpAddress(ipAddress);
            auditLog.setUserAgent(userAgent);
            auditLog.setRequestMethod(requestMethod);
            auditLog.setRequestPath(requestPath);
            auditLog.setStatusCode(statusCode);
            auditLog.setSuccess(success);

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error(
                    "Erreur lors de l'enregistrement de l'audit : action={}, user={}",
                    action,
                    userEmail,
                    e);
        }
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSuccess(
            UUID userId,
            String userEmail,
            String action,
            String entityType,
            UUID entityId,
            String description,
            String ipAddress) {

        log(
                userId,
                userEmail,
                action,
                entityType,
                entityId,
                description,
                ipAddress,
                null,
                null,
                null,
                null,
                true);
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFailure(String userEmail, String action, String description, String ipAddress) {

        log(
                null,
                userEmail,
                action,
                null,
                null,
                description,
                ipAddress,
                null,
                null,
                null,
                null,
                false);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> getAuditLogs(
            UUID userId,
            String action,
            String entityType,
            Boolean success,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            int size) {

        Page<AuditLog> pageResult =
                auditLogRepository.findAllFiltered(
                        userId,
                        action,
                        entityType,
                        success,
                        from,
                        to,
                        PageRequest.of(page, Math.min(size, 50)));

        List<AuditLogResponse> content =
                pageResult.getContent().stream().map(this::toResponse).toList();

        return PagedResponse.of(
                content,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast());
    }

    // ── Mapping interne ──

    private AuditLogResponse toResponse(AuditLog entity) {
        return AuditLogResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .userEmail(entity.getUserEmail())
                .action(entity.getAction())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .description(entity.getDescription())
                .ipAddress(entity.getIpAddress())
                .requestMethod(entity.getRequestMethod())
                .requestPath(entity.getRequestPath())
                .statusCode(entity.getStatusCode())
                .success(entity.isSuccess())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
