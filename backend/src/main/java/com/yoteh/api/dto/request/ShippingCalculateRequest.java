package com.yoteh.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public class ShippingCalculateRequest {

    @NotNull(message = "L'identifiant de la zone est obligatoire")
    private UUID zoneId;

    @NotNull(message = "Le montant de la commande est obligatoire")
    @DecimalMin(
            value = "0.0",
            inclusive = true,
            message = "Le montant de la commande ne peut pas être négatif")
    private BigDecimal orderTotal;

    @DecimalMin(value = "0.0", inclusive = true, message = "Le poids ne peut pas être négatif")
    private BigDecimal weightKg;

    // ── Getters & Setters ──

    public UUID getZoneId() {
        return zoneId;
    }

    public void setZoneId(UUID zoneId) {
        this.zoneId = zoneId;
    }

    public BigDecimal getOrderTotal() {
        return orderTotal;
    }

    public void setOrderTotal(BigDecimal orderTotal) {
        this.orderTotal = orderTotal;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }
}
