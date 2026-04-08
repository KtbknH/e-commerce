package com.yoteh.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemRequest {

    @NotNull(message = "L'identifiant du produit est obligatoire")
    private UUID productId;

    private UUID variantId;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité minimale est 1")
    private Integer quantity;
}
