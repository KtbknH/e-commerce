package com.yoteh.api.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour marquer les méthodes à auditer automatiquement. Utilisée en combinaison avec
 * {@link AuditAspect}.
 *
 * <p>Exemple :
 *
 * <pre>
 * {@code @Auditable(action = AuditActions.PRODUCT_CREATED, entityType = "Product")}
 * public ProductResponse createProduct(ProductRequest request) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /** Nom de l'action (utiliser les constantes de {@link com.yoteh.api.util.AuditActions}). */
    String action();

    /** Type d'entité concernée (ex: "Product", "Order", "User"). */
    String entityType() default "";

    /** Description statique (optionnelle). */
    String description() default "";
}
