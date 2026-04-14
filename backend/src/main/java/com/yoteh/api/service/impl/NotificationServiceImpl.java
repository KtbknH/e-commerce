package com.yoteh.api.service.impl;

import com.yoteh.api.entity.Order;
import com.yoteh.api.entity.OrderItem;
import com.yoteh.api.entity.User;
import com.yoteh.api.service.EmailService;
import com.yoteh.api.service.NotificationService;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final EmailService emailService;

    @Value("${yoteh.app.base-url:https://yoteh.com}")
    private String baseUrl;

    @Value("${yoteh.app.name:Yoteh}")
    private String appName;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ═══════════════════════════════════════════════════════════════
    //  AUTHENTIFICATION
    // ═══════════════════════════════════════════════════════════════

    @Override
    public void sendWelcomeEmail(User user, String verificationToken) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("firstName", user.getFirstName());
        vars.put("appName", appName);
        vars.put(
                "verificationUrl",
                baseUrl + "/api/v1/auth/verify-email?token=" + verificationToken);

        emailService.sendHtmlEmail(
                user.getEmail(), "Bienvenue sur " + appName + " !", "welcome", vars);
        log.info("Email de bienvenue envoyé à {}", user.getEmail());
    }

    @Override
    public void sendEmailVerification(User user, String verificationToken) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("firstName", user.getFirstName());
        vars.put("appName", appName);
        vars.put(
                "verificationUrl",
                baseUrl + "/api/v1/auth/verify-email?token=" + verificationToken);

        emailService.sendHtmlEmail(
                user.getEmail(), "Vérifiez votre adresse email — " + appName, "verify-email", vars);
        log.info("Email de vérification envoyé à {}", user.getEmail());
    }

    @Override
    public void sendPasswordResetEmail(User user, String resetToken) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("firstName", user.getFirstName());
        vars.put("appName", appName);
        vars.put("resetUrl", baseUrl + "/reset-password?token=" + resetToken);

        emailService.sendHtmlEmail(
                user.getEmail(),
                "Réinitialisation de votre mot de passe — " + appName,
                "reset-password",
                vars);
        log.info("Email de réinitialisation envoyé à {}", user.getEmail());
    }

    @Override
    public void sendPasswordChangedEmail(User user) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("firstName", user.getFirstName());
        vars.put("appName", appName);

        emailService.sendHtmlEmail(
                user.getEmail(), "Mot de passe modifié — " + appName, "password-changed", vars);
        log.info("Email confirmation changement mdp envoyé à {}", user.getEmail());
    }

    // ═══════════════════════════════════════════════════════════════
    //  COMMANDES
    // ═══════════════════════════════════════════════════════════════

    @Override
    public void sendOrderConfirmationEmail(Order order) {
        User user = order.getUser();
        Map<String, Object> vars = buildOrderVars(order);
        vars.put("title", "Confirmation de commande");

        emailService.sendHtmlEmail(
                user.getEmail(),
                "Commande " + order.getOrderNumber() + " confirmée — " + appName,
                "order-confirmed",
                vars);
        log.info(
                "Email confirmation commande {} envoyé à {}",
                order.getOrderNumber(),
                user.getEmail());
    }

    @Override
    public void sendOrderShippedEmail(Order order) {
        User user = order.getUser();
        Map<String, Object> vars = buildOrderVars(order);
        vars.put("title", "Commande expédiée");
        vars.put("trackingNumber", order.getTrackingNumber());
        vars.put(
                "hasTracking",
                order.getTrackingNumber() != null && !order.getTrackingNumber().isBlank());

        if (order.getShippedAt() != null) {
            vars.put("shippedAt", order.getShippedAt().format(DATE_FMT));
        }

        emailService.sendHtmlEmail(
                user.getEmail(),
                "Commande " + order.getOrderNumber() + " expédiée — " + appName,
                "order-shipped",
                vars);
        log.info(
                "Email expédition commande {} envoyé à {}",
                order.getOrderNumber(),
                user.getEmail());
    }

    @Override
    public void sendOrderDeliveredEmail(Order order) {
        User user = order.getUser();
        Map<String, Object> vars = buildOrderVars(order);
        vars.put("title", "Commande livrée");

        if (order.getDeliveredAt() != null) {
            vars.put("deliveredAt", order.getDeliveredAt().format(DATE_FMT));
        }

        emailService.sendHtmlEmail(
                user.getEmail(),
                "Commande " + order.getOrderNumber() + " livrée — " + appName,
                "order-delivered",
                vars);
        log.info(
                "Email livraison commande {} envoyé à {}", order.getOrderNumber(), user.getEmail());
    }

    @Override
    public void sendOrderCancelledEmail(Order order) {
        User user = order.getUser();
        Map<String, Object> vars = buildOrderVars(order);
        vars.put("title", "Commande annulée");
        vars.put("cancelReason", order.getCancelReason());

        if (order.getCancelledAt() != null) {
            vars.put("cancelledAt", order.getCancelledAt().format(DATE_FMT));
        }

        emailService.sendHtmlEmail(
                user.getEmail(),
                "Commande " + order.getOrderNumber() + " annulée — " + appName,
                "order-cancelled",
                vars);
        log.info(
                "Email annulation commande {} envoyé à {}",
                order.getOrderNumber(),
                user.getEmail());
    }

    // ═══════════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════════

    /** Construit les variables communes pour les emails de commande. */
    private Map<String, Object> buildOrderVars(Order order) {
        User user = order.getUser();
        Map<String, Object> vars = new HashMap<>();

        // Infos générales
        vars.put("firstName", user.getFirstName());
        vars.put("appName", appName);
        vars.put("orderNumber", order.getOrderNumber());
        vars.put("currency", order.getCurrency());
        vars.put("subtotal", order.getSubtotal());
        vars.put("total", order.getTotal());

        // Montants optionnels
        vars.put("discountAmount", order.getDiscountAmount());
        vars.put(
                "hasDiscount",
                order.getDiscountAmount() != null
                        && order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0);
        vars.put("shippingAmount", order.getShippingAmount());
        vars.put("taxAmount", order.getTaxAmount());

        // Adresse de livraison (snapshot)
        vars.put("shippingFirstName", order.getShippingFirstName());
        vars.put("shippingLastName", order.getShippingLastName());
        vars.put("shippingStreet", order.getShippingStreet());
        vars.put("shippingCity", order.getShippingCity());
        vars.put("shippingCountry", order.getShippingCountry());
        vars.put("shippingPhone", order.getShippingPhone());

        // Date de commande
        if (order.getCreatedAt() != null) {
            vars.put("orderDate", order.getCreatedAt().format(DATE_FMT));
        }

        // Articles
        List<Map<String, Object>> itemsList = new ArrayList<>();
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("productName", item.getProductName());
                itemMap.put("productSku", item.getProductSku());
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("unitPrice", item.getUnitPrice());
                itemMap.put("total", item.getTotal());

                if (item.getVariantInfo() != null) {
                    itemMap.put("variantInfo", item.getVariantInfo());
                }
                itemsList.add(itemMap);
            }
        }
        vars.put("items", itemsList);

        return vars;
    }
}
