package com.yoteh.api.service.payment;

import com.yoteh.api.entity.Payment;
import com.yoteh.api.entity.enums.PaymentStatus;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implémentation MTN Mobile Money (MoMo API).
 *
 * <p>TODO : Intégration réelle via l'API MTN MoMo. Documentation : https://momodeveloper.mtn.com/
 *
 * <p>Actuellement en mode stub : simule un externalId et une réponse provider sans appel réseau
 * réel. L'intégration complète sera activée en configurant les variables d'environnement
 * MTN_MONEY_API_KEY et MTN_MONEY_SUBSCRIPTION_KEY.
 */
@Slf4j
@Service("mtnMoneyProvider")
public class MtnMoneyProviderImpl implements MobileMoneyProvider {

    private static final String PROVIDER_NAME = "MTN_MONEY";
    private static final String API_BASE_URL =
            "https://sandbox.momodeveloper.mtn.com/collection/v1_0/requesttopay";

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public void initiatePayment(Payment payment) {
        log.info(
                "[MTNMoney] Initiation paiement - ref: {}, montant: {} {}, tél: {}",
                payment.getPaymentReference(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPhoneNumber());

        // Simulation : génération d'un externalId fictif pour les tests
        // À remplacer par un vrai appel POST à l'API MTN MoMo RequestToPay
        String simulatedExternalId =
                "MTN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        payment.setExternalId(simulatedExternalId);
        payment.setExternalUrl(API_BASE_URL);
        payment.setProviderResponse(
                String.format(
                        "{\"status\":\"PENDING\",\"provider\":\"MTN_MONEY\",\"externalId\":\"%s\","
                                + "\"amount\":%s,\"currency\":\"%s\"}",
                        simulatedExternalId, payment.getAmount(), payment.getCurrency()));

        log.info("[MTNMoney] ExternalId simulé assigné: {}", simulatedExternalId);
    }

    @Override
    public PaymentStatus checkPaymentStatus(String externalId) {
        // TODO : GET
        // https://sandbox.momodeveloper.mtn.com/collection/v1_0/requesttopay/{externalId}
        log.info("[MTNMoney] Vérification statut (stub) - externalId: {}", externalId);
        return PaymentStatus.PROCESSING;
    }

    @Override
    public boolean processRefund(Payment payment, BigDecimal amount, String reason) {
        // TODO : Appel API remboursement MTN MoMo (Disbursements)
        log.info(
                "[MTNMoney] Remboursement (stub) - ref: {}, montant: {}, raison: {}",
                payment.getPaymentReference(),
                amount,
                reason);
        return false;
    }
}
