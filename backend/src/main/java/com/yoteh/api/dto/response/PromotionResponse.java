package com.yoteh.api.dto.response;

import com.yoteh.api.entity.enums.PromotionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PromotionResponse {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private PromotionType type;
    private BigDecimal value;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private Integer maxUses;
    private Integer maxUsesPerUser;
    private Integer usedCount;
    private Boolean isActive;
    private Boolean isFlashSale;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private UUID applicableCategoryId;
    private String applicableCategoryName;
    private UUID applicableProductId;
    private String applicableProductName;
    private boolean valid;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Getters & Setters ──

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public Integer getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(Integer usedCount) {
        this.usedCount = usedCount;
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

    public String getApplicableCategoryName() {
        return applicableCategoryName;
    }

    public void setApplicableCategoryName(String applicableCategoryName) {
        this.applicableCategoryName = applicableCategoryName;
    }

    public UUID getApplicableProductId() {
        return applicableProductId;
    }

    public void setApplicableProductId(UUID applicableProductId) {
        this.applicableProductId = applicableProductId;
    }

    public String getApplicableProductName() {
        return applicableProductName;
    }

    public void setApplicableProductName(String applicableProductName) {
        this.applicableProductName = applicableProductName;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
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
