package com.yoteh.api.dto.response;

import java.math.BigDecimal;
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
public class OrderListResponse {

    private UUID id;
    private String orderNumber;
    private String status;
    private Integer itemCount;
    private BigDecimal total;
    private String currency;
    private String userName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
