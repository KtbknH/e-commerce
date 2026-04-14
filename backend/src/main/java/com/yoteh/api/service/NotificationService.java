package com.yoteh.api.service;

import com.yoteh.api.entity.Order;
import com.yoteh.api.entity.User;

/**
 * Service d'orchestration des notifications. Construit le contexte métier et délègue l'envoi à
 * EmailService.
 */
public interface NotificationService {

    // ── Authentification ──

    /** Email de bienvenue après inscription (avec lien de vérification). */
    void sendWelcomeEmail(User user, String verificationToken);

    /** Email de vérification d'adresse email. */
    void sendEmailVerification(User user, String verificationToken);

    /** Email de réinitialisation de mot de passe. */
    void sendPasswordResetEmail(User user, String resetToken);

    /** Email de confirmation : mot de passe modifié avec succès. */
    void sendPasswordChangedEmail(User user);

    // ── Commandes ──

    /** Email de confirmation de commande (après paiement validé). */
    void sendOrderConfirmationEmail(Order order);

    /** Email de notification d'expédition (avec tracking si disponible). */
    void sendOrderShippedEmail(Order order);

    /** Email de livraison confirmée. */
    void sendOrderDeliveredEmail(Order order);

    /** Email d'annulation de commande. */
    void sendOrderCancelledEmail(Order order);
}
