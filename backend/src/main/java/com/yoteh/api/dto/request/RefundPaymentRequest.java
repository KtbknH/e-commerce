package com.yoteh.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class RefundPaymentRequest {

    @NotBlank(message = "La raison du remboursement est obligatoire")
    private String reason;

    /** Montant à rembourser. Si null : remboursement total du paiement. */
    @DecimalMin(value = "1.0", message = "Le montant du remboursement doit être supérieur à 0")
    private BigDecimal amount;
}
