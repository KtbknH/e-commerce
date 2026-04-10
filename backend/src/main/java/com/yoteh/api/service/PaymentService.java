package com.yoteh.api.service;

import com.yoteh.api.dto.request.InitPaymentRequest;
import com.yoteh.api.dto.request.PaymentCallbackRequest;
import com.yoteh.api.dto.request.RefundPaymentRequest;
import com.yoteh.api.dto.response.PaymentResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import java.util.UUID;

public interface PaymentService {

    /** Initier un paiement pour une commande PENDING. */
    PaymentResponse initiatePayment(InitPaymentRequest request, UUID userId);

    /** Confirmer un paiement (retour depuis la page provider ou appel front). */
    PaymentResponse confirmPayment(String paymentReference, PaymentCallbackRequest request);

    /** Traiter un webhook entrant d'un provider (Orange Money, MTN, etc.). */
    void handleWebhook(String provider, String payload, String signature);

    /** Vérifier le statut d'un paiement (polling provider si PROCESSING). */
    PaymentResponse verifyPayment(String paymentReference, UUID userId);

    /** Historique paginé des paiements de l'utilisateur connecté. */
    PagedResponse<PaymentResponse> getPaymentHistory(UUID userId, int page, int size);

    /** Détail d'un paiement (accessible au propriétaire). */
    PaymentResponse getPaymentDetail(String paymentReference, UUID userId);

    // ─── Admin ───────────────────────────────────────────────────────────────

    /** Liste paginée de tous les paiements avec filtres (admin). */
    PagedResponse<PaymentResponse> getAllPayments(
            int page, int size, String status, String provider, String sortDir);

    /** Détail d'un paiement sans vérification de propriété (admin). */
    PaymentResponse adminGetPayment(String paymentReference);

    /** Rembourser un paiement COMPLETED (admin). */
    PaymentResponse refundPayment(String paymentReference, RefundPaymentRequest request);
}
