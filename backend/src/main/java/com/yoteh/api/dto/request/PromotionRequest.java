package com.yoteh.api.dto.request;

import com.yoteh.api.entity.enums.PromotionType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PromotionRequest {

    @NotBlank(message = "Le code est obligatoire")
    @Size(min = 3, max = 50, message = "Le code doit contenir entre 3 et 50 caractères")
    @Pattern(
            regexp = "^[A-Z0-9_-]+$",
            message =
                    "Le code ne peut contenir que des lettres majuscules, chiffres, tirets et underscores")
    private String code;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 255, message = "Le nom ne peut pas dépasser 255 caractères")
    private String name;

    private String description;

    @NotNull(message = "Le type est obligatoire")
    private PromotionType type;

    @NotNull(message = "La valeur est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "La valeur doit être positive")
    private BigDecimal value;

    @DecimalMin(value = "0.0", message = "Le montant minimum doit être positif")
    private BigDecimal minOrderAmount;

    @DecimalMin(value = "0.0", message = "La réduction maximum doit être positive")
    private BigDecimal maxDiscountAmount;

    @Min(value = 1, message = "Le nombre maximum d'utilisations doit être au moins 1")
    private Integer maxUses;

    @Min(value = 1, message = "Le nombre max par utilisateur doit être au moins 1")
    private Integer maxUsesPerUser = 1;

    private Boolean isActive = true;

    private Boolean isFlashSale = false;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDateTime startsAt;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime endsAt;

    private UUID applicableCategoryId;

    private UUID applicableProductId;

    // ── Getters & Setters ──

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public PromotionType getType() {
        return type;
    }

    public void setType(PromotionType type) {
        this.type = type;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getMinOrderAmount() {
        return minOrderAmount;
    }

    public void setMinOrderAmount(BigDecimal minOrderAmount) {
        this.minOrderAmount = minOrderAmount;
    }

    public BigDecimal getMaxDiscountAmount() {
        return maxDiscountAmount;
    }

    public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) {
        this.maxDiscountAmount = maxDiscountAmount;
    }

    public Integer getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(Integer maxUses) {
        this.maxUses = maxUses;
    }

    public Integer getMaxUsesPerUser() {
        return maxUsesPerUser;
    }

    public void setMaxUsesPerUser(Integer maxUsesPerUser) {
        this.maxUsesPerUser = maxUsesPerUser;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsFlashSale() {
        return isFlashSale;
    }

    public void setIsFlashSale(Boolean isFlashSale) {
        this.isFlashSale = isFlashSale;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(LocalDateTime startsAt) {
        this.startsAt = startsAt;
    }

    public LocalDateTime getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(LocalDateTime endsAt) {
        this.endsAt = endsAt;
    }

    public UUID getApplicableCategoryId() {
        return applicableCategoryId;
    }

    public void setApplicableCategoryId(UUID applicableCategoryId) {
        this.applicableCategoryId = applicableCategoryId;
    }

    public UUID getApplicableProductId() {
        return applicableProductId;
    }

    public void setApplicableProductId(UUID applicableProductId) {
        this.applicableProductId = applicableProductId;
    }
}
