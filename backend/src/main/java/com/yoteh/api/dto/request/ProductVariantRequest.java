package com.yoteh.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
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
public class ProductVariantRequest {

    // Pour update : si null, on crée une nouvelle variante
    private UUID id;

    @Size(max = 50, message = "La taille ne doit pas dépasser 50 caractères")
    private String size;

    @Size(max = 50, message = "La couleur ne doit pas dépasser 50 caractères")
    private String color;

    @Size(max = 50, message = "Le SKU ne doit pas dépasser 50 caractères")
    private String sku;

    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit être supérieur à 0")
    private BigDecimal price;

    private BigDecimal compareAtPrice;

    @Min(value = 0, message = "Le stock ne peut pas être négatif")
    private Integer stockQuantity;

    private Boolean isActive;
}
