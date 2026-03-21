package com.yoteh.api.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts", indexes = {
        @Index(name = "idx_carts_user_id", columnList = "user_id", unique = true)
})
public class Cart extends AbstractAuditEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "promotion_code", length = 50)
    private String promotionCode;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    // ── Constructeurs ──

    public Cart() {}

    public Cart(User user) {
        this.user = user;
    }

    // ── Getters & Setters ──

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }

    public String getPromotionCode() { return promotionCode; }
    public void setPromotionCode(String promotionCode) { this.promotionCode = promotionCode; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    // ── Méthodes utilitaires ──

    public BigDecimal getSubtotal() {
        return items.stream()
                .map(CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotal() {
        return getSubtotal().subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
    }

    public int getItemCount() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }
}
