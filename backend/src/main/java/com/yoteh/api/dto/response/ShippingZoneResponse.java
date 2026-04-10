package com.yoteh.api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ShippingZoneResponse {

    private UUID id;
    private String name;
    private String description;
    private String cities;
    private String country;
    private BigDecimal baseFee;
    private BigDecimal perKgFee;
    private BigDecimal freeShippingThreshold;
    private Integer estimatedDaysMin;
    private Integer estimatedDaysMax;
    private String estimatedDelivery;
    private Boolean isActive;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Getters & Setters ──

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public String getEstimatedDelivery() {
        return estimatedDelivery;
    }

    public void setEstimatedDelivery(String estimatedDelivery) {
        this.estimatedDelivery = estimatedDelivery;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
