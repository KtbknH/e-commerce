package com.yoteh.api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    private UUID id;
    private UUID userId;
    private List<CartItemResponse> items;
    private Integer itemCount;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal total;
    private String promotionCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
