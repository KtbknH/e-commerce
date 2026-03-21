package com.yoteh.api.util;

public final class Constants {

    private Constants() {}

    // ─── Rôles utilisateur ────────────────────────────────────────
    public static final String ROLE_CLIENT = "CLIENT";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MANAGER = "MANAGER";

    // ─── Statuts commande ─────────────────────────────────────────
    public static final String ORDER_PENDING = "PENDING";
    public static final String ORDER_PAID = "PAID";
    public static final String ORDER_PREPARING = "PREPARING";
    public static final String ORDER_SHIPPED = "SHIPPED";
    public static final String ORDER_DELIVERED = "DELIVERED";
    public static final String ORDER_CANCELLED = "CANCELLED";

    // ─── Statuts paiement ─────────────────────────────────────────
    public static final String PAYMENT_PENDING = "PENDING";
    public static final String PAYMENT_COMPLETED = "COMPLETED";
    public static final String PAYMENT_FAILED = "FAILED";
    public static final String PAYMENT_REFUNDED = "REFUNDED";

    // ─── Méthodes de paiement ─────────────────────────────────────
    public static final String PAY_ORANGE_MONEY = "ORANGE_MONEY";
    public static final String PAY_MTN_MONEY = "MTN_MONEY";
    public static final String PAY_CARD = "CARD";

    // ─── Statuts produit ──────────────────────────────────────────
    public static final String PRODUCT_ACTIVE = "ACTIVE";
    public static final String PRODUCT_DRAFT = "DRAFT";
    public static final String PRODUCT_ARCHIVED = "ARCHIVED";

    // ─── Devises ──────────────────────────────────────────────────
    public static final String CURRENCY_XOF = "XOF";
    public static final String CURRENCY_CDF = "CDF";
    public static final String CURRENCY_USD = "USD";
    public static final String CURRENCY_EUR = "EUR";

    // ─── Pagination ───────────────────────────────────────────────
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIR = "desc";

    // ─── Fidélité ─────────────────────────────────────────────────
    public static final String LOYALTY_BRONZE = "BRONZE";
    public static final String LOYALTY_SILVER = "SILVER";
    public static final String LOYALTY_GOLD = "GOLD";
    public static final String LOYALTY_PLATINUM = "PLATINUM";
}
