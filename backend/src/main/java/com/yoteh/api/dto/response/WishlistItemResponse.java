package com.yoteh.api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItemResponse {

    private UUID id;
    private LocalDateTime addedAt;

    // ─── Produit ───────────────────────────────────────────────
    private UUID productId;
    private String productName;
    private String productSlug;
    private BigDecimal productPrice;
    private BigDecimal productCompareAtPrice;
    private String productCurrency;
    private String productPrimaryImageUrl;
    private Boolean productInStock;
    private Boolean productIsFeatured;
    private Integer productDiscountPercent;

    // ─── Catégorie ─────────────────────────────────────────────
    private String categoryName;
    private UUID categoryId;
}
