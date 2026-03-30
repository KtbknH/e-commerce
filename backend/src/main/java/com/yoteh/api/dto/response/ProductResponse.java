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
public class ProductResponse {

    private UUID id;
    private String name;
    private String slug;
    private String description;
    private String shortDescription;
    private String sku;
    private String barcode;

    // ─── Prix ───────────────────────────────────────────────
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private BigDecimal costPrice;
    private String currency;
    private Integer discountPercent;

    // ─── Stock ──────────────────────────────────────────────
    private Integer stockQuantity;
    private Integer lowStockThreshold;
    private Boolean inStock;

    // ─── Détails ────────────────────────────────────────────
    private BigDecimal weight;
    private Boolean isActive;
    private Boolean isFeatured;

    // ─── Catégorie ──────────────────────────────────────────
    private UUID categoryId;
    private String categoryName;
    private String categorySlug;

    // ─── Images ─────────────────────────────────────────────
    private String primaryImageUrl;
    private List<ProductImageResponse> images;

    // ─── Variantes ──────────────────────────────────────────
    private List<ProductVariantResponse> variants;
    private int variantCount;

    // ─── Avis ───────────────────────────────────────────────
    private Double averageRating;
    private Integer reviewCount;

    // ─── Dates ──────────────────────────────────────────────
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
