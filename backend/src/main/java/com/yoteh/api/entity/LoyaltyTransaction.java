package com.yoteh.api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "loyalty_transactions", indexes = {
        @Index(name = "idx_loyalty_tx_user_id", columnList = "user_id"),
        @Index(name = "idx_loyalty_tx_order_id", columnList = "order_id"),
        @Index(name = "idx_loyalty_tx_type", columnList = "type")
})
public class LoyaltyTransaction extends AbstractAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "type", nullable = false, length = 20)
    private String type; // EARN, REDEEM, EXPIRE, ADJUST

    @Column(name = "points", nullable = false)
    private Integer points;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "reference", length = 100)
    private String reference;

    // ── Constructeurs ──

    public LoyaltyTransaction() {}

    // ── Getters & Setters ──

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    public Integer getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(Integer balanceAfter) { this.balanceAfter = balanceAfter; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
}
