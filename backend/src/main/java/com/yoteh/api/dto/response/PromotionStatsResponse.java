package com.yoteh.api.dto.response;

import java.util.UUID;

public class PromotionStatsResponse {

    private UUID promotionId;
    private String code;
    private String name;
    private Integer usedCount;
    private Integer maxUses;
    private Double usageRate; // pourcentage : usedCount / maxUses * 100 (null si illimité)
    private boolean isActive;
    private boolean isFlashSale;

    // ── Getters & Setters ──

    public UUID getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(UUID promotionId) {
        this.promotionId = promotionId;
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

    public Integer getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(Integer usedCount) {
        this.usedCount = usedCount;
    }

    public Integer getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(Integer maxUses) {
        this.maxUses = maxUses;
    }

    public Double getUsageRate() {
        return usageRate;
    }

    public void setUsageRate(Double usageRate) {
        this.usageRate = usageRate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isFlashSale() {
        return isFlashSale;
    }

    public void setFlashSale(boolean flashSale) {
        isFlashSale = flashSale;
    }
}
