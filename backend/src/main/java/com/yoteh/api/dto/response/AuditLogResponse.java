package com.yoteh.api.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private UUID id;
    private UUID userId;
    private String userEmail;
    private String action;
    private String entityType;
    private UUID entityId;
    private String description;
    private String ipAddress;
    private String requestMethod;
    private String requestPath;
    private Integer statusCode;
    private boolean success;
    private LocalDateTime createdAt;
}
