package com.yoteh.api.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "wishlist_items",
        indexes = {
            @Index(name = "idx_wishlist_user_id", columnList = "user_id"),
            @Index(name = "idx_wishlist_product_id", columnList = "product_id")
        },
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_wishlist_user_product",
                    columnNames = {"user_id", "product_id"})
        })
public class WishlistItem extends AbstractAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // ── Constructeurs ──

    public WishlistItem() {}

    public WishlistItem(User user, Product product) {
        this.user = user;
        this.product = product;
    }

    // ── Getters & Setters ──

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
