package com.yoteh.api.service.payment;

import com.yoteh.api.entity.Payment;
import com.yoteh.api.entity.enums.PaymentStatus;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implémentation Orange Money WebPay.
 *
 * <p>TODO : Intégration réelle via l'API Orange Money WebPay. Documentation :
 * https://developer.orange.com/apis/orange-money-webpay-ci/getting-started
 *
 * <p>Actuellement en mode stub : simule un externalId et une réponse provider sans appel réseau
 * réel. L'intégration complète sera activée en configurant les variables d'environnement
 * ORANGE_MONEY_CLIENT_ID et ORANGE_MONEY_CLIENT_SECRET.
 */
@Slf4j
@Service("orangeMoneyProvider")
public class OrangeMoneyProviderImpl implements MobileMoneyProvider {

    private static final String PROVIDER_NAME = "ORANGE_MONEY";
    private static final String API_BASE_URL =
            "https://api.orange.com/orange-money-webpay/dev/v1/webpayment";

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public void initiatePayment(Payment payment) {
        log.info(
                "[OrangeMoney] Initiation paiement - ref: {}, montant: {} {}, tél: {}",
                payment.getPaymentReference(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPhoneNumber());

        // Simulation : génération d'un externalId fictif pour les tests
        // À remplacer par un vrai appel POST à l'API Orange Money
        String simulatedExternalId =
                "OM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        payment.setExternalId(simulatedExternalId);
        payment.setExternalUrl(API_BASE_URL);
        payment.setProviderResponse(
                String.format(
                        "{\"status\":\"PENDING\",\"provider\":\"ORANGE_MONEY\",\"externalId\":\"%s\","
                                + "\"amount\":%s,\"currency\":\"%s\"}",
                        simulatedExternalId, payment.getAmount(), payment.getCurrency()));

        log.info("[OrangeMoney] ExternalId simulé assigné: {}", simulatedExternalId);
    }

    @Override
    public PaymentStatus checkPaymentStatus(String externalId) {
        // TODO : GET https://api.orange.com/orange-money-webpay/dev/v1/webpayment/{externalId}
        log.info("[OrangeMoney] Vérification statut (stub) - externalId: {}", externalId);
        return PaymentStatus.PROCESSING;
    }

    @Override
    public boolean processRefund(Payment payment, BigDecimal amount, String reason) {
        // TODO : Appel API remboursement Orange Money
        log.info(
                "[OrangeMoney] Remboursement (stub) - ref: {}, montant: {}, raison: {}",
                payment.getPaymentReference(),
                amount,
                reason);
        return false;
    }
}
