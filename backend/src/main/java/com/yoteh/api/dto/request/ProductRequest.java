package com.yoteh.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
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
public class ProductRequest {

    @NotBlank(message = "Le nom du produit est obligatoire")
    @Size(min = 2, max = 255, message = "Le nom doit contenir entre 2 et 255 caractères")
    private String name;

    @Size(max = 5000, message = "La description ne doit pas dépasser 5000 caractères")
    private String description;

    @Size(max = 500, message = "La description courte ne doit pas dépasser 500 caractères")
    private String shortDescription;

    @NotNull(message = "La catégorie est obligatoire")
    private UUID categoryId;

    @Size(max = 50, message = "Le SKU ne doit pas dépasser 50 caractères")
    private String sku;

    private String barcode;

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit être supérieur à 0")
    private BigDecimal price;

    private BigDecimal compareAtPrice;

    private BigDecimal costPrice;

    @Size(max = 3, message = "La devise ne doit pas dépasser 3 caractères")
    private String currency;

    @Min(value = 0, message = "Le stock ne peut pas être négatif")
    private Integer stockQuantity;

    @Min(value = 0, message = "Le seuil de stock bas ne peut pas être négatif")
    private Integer lowStockThreshold;

    private BigDecimal weight;

    private Boolean isActive;

    private Boolean isFeatured;

    // ─── Images (URLs) ──────────────────────────────────────
    private List<String> imageUrls;

    // ─── Variantes ──────────────────────────────────────────
    @Valid private List<ProductVariantRequest> variants;
}
