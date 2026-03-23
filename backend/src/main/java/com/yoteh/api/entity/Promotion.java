package com.yoteh.api.entity;

import com.yoteh.api.entity.enums.PromotionType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions", indexes = {
        @Index(name = "idx_promotions_code", columnList = "code", unique = true),
        @Index(name = "idx_promotions_is_active", columnList = "is_active"),
        @Index(name = "idx_promotions_starts_at", columnList = "starts_at"),
        @Index(name = "idx_promotions_ends_at", columnList = "ends_at")
})
public class Promotion extends AbstractAuditEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private PromotionType type;

    @Column(name = "value", nullable = false, precision = 12, scale = 2)
    private BigDecimal value;

    @Column(name = "min_order_amount", precision = 12, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(name = "max_discount_amount", precision = 12, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "max_uses")
    private Integer maxUses;

    @Column(name = "max_uses_per_user")
    private Integer maxUsesPerUser = 1;

    @Column(name = "used_count", nullable = false)
    private Integer usedCount = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_flash_sale", nullable = false)
    private Boolean isFlashSale = false;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private LocalDateTime endsAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category applicableCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product applicableProduct;

    // ── Constructeurs ──

    public Promotion() {}

    // ── Getters & Setters ──

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public PromotionType getType() { return type; }
    public void setType(PromotionType type) { this.type = type; }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public BigDecimal getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(BigDecimal minOrderAmount) { this.minOrderAmount = minOrderAmount; }

    public BigDecimal getMaxDiscountAmount() { return maxDiscountAmount; }
    public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) { this.maxDiscountAmount = maxDiscountAmount; }

    public Integer getMaxUses() { return maxUses; }
    public void setMaxUses(Integer maxUses) { this.maxUses = maxUses; }

    public Integer getMaxUsesPerUser() { return maxUsesPerUser; }
    public void setMaxUsesPerUser(Integer maxUsesPerUser) { this.maxUsesPerUser = maxUsesPerUser; }

    public Integer getUsedCount() { return usedCount; }
    public void setUsedCount(Integer usedCount) { this.usedCount = usedCount; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getIsFlashSale() { return isFlashSale; }
    public void setIsFlashSale(Boolean isFlashSale) { this.isFlashSale = isFlashSale; }

    public LocalDateTime getStartsAt() { return startsAt; }
    public void setStartsAt(LocalDateTime startsAt) { this.startsAt = startsAt; }

    public LocalDateTime getEndsAt() { return endsAt; }
    public void setEndsAt(LocalDateTime endsAt) { this.endsAt = endsAt; }

    public Category getApplicableCategory() { return applicableCategory; }
    public void setApplicableCategory(Category applicableCategory) { this.applicableCategory = applicableCategory; }

    public Product getApplicableProduct() { return applicableProduct; }
    public void setApplicableProduct(Product applicableProduct) { this.applicableProduct = applicableProduct; }

    // ── Méthodes utilitaires ──

    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return isActive
                && now.isAfter(startsAt)
                && now.isBefore(endsAt)
                && (maxUses == null || usedCount < maxUses);
    }
}
