package com.yoteh.api.repository;

import com.yoteh.api.entity.AuditLog;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<AuditLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);

    @Query(
            "SELECT a FROM AuditLog a WHERE "
                    + "(:userId IS NULL OR a.userId = :userId) "
                    + "AND (:action IS NULL OR a.action = :action) "
                    + "AND (:entityType IS NULL OR a.entityType = :entityType) "
                    + "AND (:success IS NULL OR a.success = :success) "
                    + "AND (:from IS NULL OR a.createdAt >= :from) "
                    + "AND (:to IS NULL OR a.createdAt <= :to) "
                    + "ORDER BY a.createdAt DESC")
    Page<AuditLog> findAllFiltered(
            @Param("userId") UUID userId,
            @Param("action") String action,
            @Param("entityType") String entityType,
            @Param("success") Boolean success,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    long countByActionAndCreatedAtAfter(String action, LocalDateTime after);

    long countByActionAndSuccessAndCreatedAtAfter(
            String action, boolean success, LocalDateTime after);

    @Query(
            "SELECT a FROM AuditLog a WHERE a.action = 'LOGIN_FAILED' "
                    + "AND a.ipAddress = :ip "
                    + "AND a.createdAt >= :since "
                    + "ORDER BY a.createdAt DESC")
    java.util.List<AuditLog> findRecentFailedLoginsByIp(
            @Param("ip") String ip, @Param("since") LocalDateTime since);
}
