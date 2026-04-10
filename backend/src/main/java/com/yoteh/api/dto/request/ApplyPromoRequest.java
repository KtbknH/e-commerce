package com.yoteh.api.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ApplyPromoRequest {

    @NotBlank(message = "Le code promo est obligatoire")
    private String code;

    @NotNull(message = "Le montant de la commande est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le montant doit être positif")
    private BigDecimal orderAmount;

    // ── Getters & Setters ──

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(BigDecimal orderAmount) {
        this.orderAmount = orderAmount;
    }
}
