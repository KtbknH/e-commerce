package com.yoteh.api.dto.response;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    private UUID id;
    private UUID productId;
    private String productName;
    private String productSku;
    private String productImage;
    private UUID variantId;
    private String variantInfo;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountAmount;
    private BigDecimal total;
    private String status;
}
