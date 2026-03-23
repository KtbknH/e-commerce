package com.yoteh.api.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product_variants", indexes = {
        @Index(name = "idx_product_variants_product_id", columnList = "product_id"),
        @Index(name = "idx_product_variants_sku", columnList = "sku", unique = true)
})
public class ProductVariant extends AbstractAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "sku", nullable = false, unique = true, length = 100)
    private String sku;

    @Column(name = "size", length = 50)
    private String size;

    @Column(name = "color", length = 50)
    private String color;

    @Column(name = "color_hex", length = 7)
    private String colorHex;

    @Column(name = "material", length = 100)
    private String material;

    @Column(name = "style", length = 100)
    private String style;

    @Column(name = "price", precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "compare_at_price", precision = 12, scale = 2)
    private BigDecimal compareAtPrice;

    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    @Column(name = "low_stock_threshold", nullable = false)
    private Integer lowStockThreshold = 3;

    @Column(name = "weight", precision = 8, scale = 2)
    private BigDecimal weight;

    @Column(name = "image", length = 500)
    private String image;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    // ── Constructeurs ──

    public ProductVariant() {}

    // ── Getters & Setters ──

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }

    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getCompareAtPrice() { return compareAtPrice; }
    public void setCompareAtPrice(BigDecimal compareAtPrice) { this.compareAtPrice = compareAtPrice; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Integer getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(Integer lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }

    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    // ── Méthodes utilitaires ──

    public BigDecimal getEffectivePrice() {
        return this.price != null ? this.price : this.product.getPrice();
    }

    public boolean isLowStock() {
        return this.stock <= this.lowStockThreshold;
    }
}
