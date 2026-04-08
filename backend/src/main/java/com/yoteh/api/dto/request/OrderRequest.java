package com.yoteh.api.dto.request;

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
public class OrderRequest {

    @NotNull(message = "L'adresse de livraison est obligatoire")
    private UUID shippingAddressId;

    private String customerNote;

    private String currency;
}
