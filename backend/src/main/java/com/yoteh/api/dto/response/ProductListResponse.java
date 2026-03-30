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
public class ProductListResponse {

    private UUID id;
    private String name;
    private String slug;
    private String shortDescription;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private String currency;
    private Integer discountPercent;
    private Boolean inStock;
    private Boolean isFeatured;
    private String primaryImageUrl;
    private String categoryName;
    private UUID categoryId;
    private Double averageRating;
    private Integer reviewCount;
    private int variantCount;
}
