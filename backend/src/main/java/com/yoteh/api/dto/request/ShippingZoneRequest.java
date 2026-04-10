package com.yoteh.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class ShippingZoneRequest {

    @NotBlank(message = "Le nom de la zone est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String name;

    private String description;

    private String cities;

    @NotBlank(message = "Le pays est obligatoire")
    @Size(max = 100, message = "Le pays ne peut pas dépasser 100 caractères")
    private String country;

    @NotNull(message = "Les frais de base sont obligatoires")
    @DecimalMin(
            value = "0.0",
            inclusive = true,
            message = "Les frais de base ne peuvent pas être négatifs")
    private BigDecimal baseFee;

    @DecimalMin(
            value = "0.0",
            inclusive = true,
            message = "Les frais par kg ne peuvent pas être négatifs")
    private BigDecimal perKgFee;

    @DecimalMin(
            value = "0.0",
            inclusive = true,
            message = "Le seuil de livraison gratuite ne peut pas être négatif")
    private BigDecimal freeShippingThreshold;

    @Min(value = 0, message = "Le délai minimum ne peut pas être négatif")
    private Integer estimatedDaysMin;

    @Min(value = 0, message = "Le délai maximum ne peut pas être négatif")
    private Integer estimatedDaysMax;

    private Boolean isActive = true;

    @Min(value = 0, message = "L'ordre de tri ne peut pas être négatif")
    private Integer sortOrder = 0;

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
}
