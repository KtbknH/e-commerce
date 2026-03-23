package com.yoteh.api.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "shipping_zones",
        indexes = {
            @Index(name = "idx_shipping_zones_name", columnList = "name"),
            @Index(name = "idx_shipping_zones_is_active", columnList = "is_active")
        })
public class ShippingZone extends AbstractAuditEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "cities", columnDefinition = "TEXT")
    private String cities;

    @Column(name = "country", nullable = false, length = 100)
    private String country = "Congo";

    @Column(name = "base_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseFee;

    @Column(name = "per_kg_fee", precision = 12, scale = 2)
    private BigDecimal perKgFee = BigDecimal.ZERO;

    @Column(name = "free_shipping_threshold", precision = 12, scale = 2)
    private BigDecimal freeShippingThreshold;

    @Column(name = "estimated_days_min")
    private Integer estimatedDaysMin;

    @Column(name = "estimated_days_max")
    private Integer estimatedDaysMax;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    // ── Constructeurs ──

    public ShippingZone() {}

    // ── Getters & Setters ──

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCities() {
        return cities;
    }

    public void setCities(String cities) {
        this.cities = cities;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public BigDecimal getBaseFee() {
        return baseFee;
    }

    public void setBaseFee(BigDecimal baseFee) {
        this.baseFee = baseFee;
    }

    public BigDecimal getPerKgFee() {
        return perKgFee;
    }

    public void setPerKgFee(BigDecimal perKgFee) {
        this.perKgFee = perKgFee;
    }

    public BigDecimal getFreeShippingThreshold() {
        return freeShippingThreshold;
    }

    public void setFreeShippingThreshold(BigDecimal freeShippingThreshold) {
        this.freeShippingThreshold = freeShippingThreshold;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    // ── Méthodes utilitaires ──

    public BigDecimal calculateFee(BigDecimal orderWeight, BigDecimal orderTotal) {
        if (freeShippingThreshold != null && orderTotal.compareTo(freeShippingThreshold) >= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal fee = baseFee;
        if (perKgFee != null && orderWeight != null) {
            fee = fee.add(perKgFee.multiply(orderWeight));
        }
        return fee;
    }
}
