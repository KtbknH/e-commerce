package com.yoteh.api.service.payment;

import com.yoteh.api.entity.Payment;
import com.yoteh.api.entity.enums.PaymentStatus;
import java.math.BigDecimal;

/**
 * Interface d'abstraction pour les providers Mobile Money (Orange Money, MTN Money).
 *
 * <p>Chaque implémentation gère l'intégration avec un provider spécifique. Les méthodes sont des
 * stubs pour l'instant (Chat 7) ; l'intégration API réelle est prévue dans une phase ultérieure.
 */
public interface MobileMoneyProvider {

    /** Nom lisible du provider (ex : "ORANGE_MONEY"). */
    String getProviderName();

    /**
     * Initie un paiement auprès du provider. Doit renseigner {@code payment.externalId}, {@code
     * payment.externalUrl} et {@code payment.providerResponse}.
     */
    void initiatePayment(Payment payment);

    /**
     * Vérifie le statut d'un paiement auprès du provider (polling). Retourne le nouveau statut
     * Yoteh correspondant.
     */
    PaymentStatus checkPaymentStatus(String externalId);

    /**
     * Demande un remboursement auprès du provider.
     *
     * @return {@code true} si le remboursement a été accepté par le provider
     */
    boolean processRefund(Payment payment, BigDecimal amount, String reason);
}
