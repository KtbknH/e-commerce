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
public class CartItemResponse {

    private UUID id;
    private UUID productId;
    private String productName;
    private String productSlug;
    private String productImage;
    private String productSku;
    private UUID variantId;
    private String variantSku;
    private String size;
    private String color;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
    private Integer availableStock;
    private Boolean inStock;
    private LocalDateTime createdAt;
}
