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
public class LoyaltyTransactionResponse {

    private UUID id;
    private UUID userId;
    private String userFullName;
    private UUID orderId;
    private String type;
    private Integer points;
    private Integer balanceAfter;
    private String description;
    private String reference;
    private LocalDateTime createdAt;
}
