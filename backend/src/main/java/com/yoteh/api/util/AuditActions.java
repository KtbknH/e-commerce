package com.yoteh.api.util;

/**
 * Constantes pour les actions d'audit. Utilisées dans AuditService.log() pour garantir la cohérence
 * des noms d'actions.
 */
public final class AuditActions {

    private AuditActions() {}

    // ─── Authentification ───
    public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String LOGIN_FAILED = "LOGIN_FAILED";
    public static final String LOGOUT = "LOGOUT";
    public static final String REGISTER = "REGISTER";
    public static final String PASSWORD_RESET_REQUEST = "PASSWORD_RESET_REQUEST";
    public static final String PASSWORD_RESET = "PASSWORD_RESET";
    public static final String PASSWORD_CHANGED = "PASSWORD_CHANGED";
    public static final String EMAIL_VERIFIED = "EMAIL_VERIFIED";

    // ─── Utilisateurs (admin) ───
    public static final String USER_BANNED = "USER_BANNED";
    public static final String USER_UNBANNED = "USER_UNBANNED";
    public static final String USER_ROLE_CHANGED = "USER_ROLE_CHANGED";
    public static final String USER_DELETED = "USER_DELETED";

    // ─── Produits (admin) ───
    public static final String PRODUCT_CREATED = "PRODUCT_CREATED";
    public static final String PRODUCT_UPDATED = "PRODUCT_UPDATED";
    public static final String PRODUCT_DELETED = "PRODUCT_DELETED";

    // ─── Catégories (admin) ───
    public static final String CATEGORY_CREATED = "CATEGORY_CREATED";
    public static final String CATEGORY_UPDATED = "CATEGORY_UPDATED";
    public static final String CATEGORY_DELETED = "CATEGORY_DELETED";

    // ─── Commandes (admin) ───
    public static final String ORDER_STATUS_CHANGED = "ORDER_STATUS_CHANGED";
    public static final String ORDER_CANCELLED = "ORDER_CANCELLED";
    public static final String ORDER_REFUNDED = "ORDER_REFUNDED";

    // ─── Paiements ───
    public static final String PAYMENT_INITIATED = "PAYMENT_INITIATED";
    public static final String PAYMENT_CONFIRMED = "PAYMENT_CONFIRMED";
    public static final String PAYMENT_FAILED = "PAYMENT_FAILED";
    public static final String PAYMENT_REFUNDED = "PAYMENT_REFUNDED";

    // ─── Promotions (admin) ───
    public static final String PROMOTION_CREATED = "PROMOTION_CREATED";
    public static final String PROMOTION_UPDATED = "PROMOTION_UPDATED";
    public static final String PROMOTION_DELETED = "PROMOTION_DELETED";

    // ─── Shipping (admin) ───
    public static final String SHIPPING_ZONE_CREATED = "SHIPPING_ZONE_CREATED";
    public static final String SHIPPING_ZONE_UPDATED = "SHIPPING_ZONE_UPDATED";
    public static final String SHIPPING_ZONE_DELETED = "SHIPPING_ZONE_DELETED";

    // ─── Reviews (admin) ───
    public static final String REVIEW_APPROVED = "REVIEW_APPROVED";
    public static final String REVIEW_REJECTED = "REVIEW_REJECTED";
    public static final String REVIEW_DELETED = "REVIEW_DELETED";

    // ─── Loyalty (admin) ───
    public static final String LOYALTY_POINTS_ADJUSTED = "LOYALTY_POINTS_ADJUSTED";

    // ─── Export ───
    public static final String EXPORT_CSV = "EXPORT_CSV";
}
