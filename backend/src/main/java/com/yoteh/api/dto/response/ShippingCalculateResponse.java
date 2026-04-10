package com.yoteh.api.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public class ShippingCalculateResponse {

    private UUID zoneId;
    private String zoneName;
    private BigDecimal shippingFee;
    private Boolean isFreeShipping;
    private BigDecimal orderTotal;
    private BigDecimal weightKg;
    private Integer estimatedDaysMin;
    private Integer estimatedDaysMax;
    private String estimatedDelivery;
    private BigDecimal freeShippingThreshold;

    // ── Getters & Setters ──

    public UUID getZoneId() {
        return zoneId;
    }

    public void setZoneId(UUID zoneId) {
        this.zoneId = zoneId;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }

    public Boolean getIsFreeShipping() {
        return isFreeShipping;
    }

    public void setIsFreeShipping(Boolean isFreeShipping) {
        this.isFreeShipping = isFreeShipping;
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

    public Integer getEstimatedDaysMin() {
        return estimatedDaysMin;
    }

    public void setEstimatedDaysMin(Integer estimatedDaysMin) {
        this.estimatedDaysMin = estimatedDaysMin;
    }

    public Integer getEstimatedDaysMax() {
        return estimatedDaysMax;
    }

    public void setEstimatedDaysMax(Integer estimatedDaysMax) {
        this.estimatedDaysMax = estimatedDaysMax;
    }

    public String getEstimatedDelivery() {
        return estimatedDelivery;
    }

    public void setEstimatedDelivery(String estimatedDelivery) {
        this.estimatedDelivery = estimatedDelivery;
    }

    public BigDecimal getFreeShippingThreshold() {
        return freeShippingThreshold;
    }

    public void setFreeShippingThreshold(BigDecimal freeShippingThreshold) {
        this.freeShippingThreshold = freeShippingThreshold;
    }
}
