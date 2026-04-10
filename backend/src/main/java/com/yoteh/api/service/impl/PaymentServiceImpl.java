package com.yoteh.api.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoteh.api.dto.request.InitPaymentRequest;
import com.yoteh.api.dto.request.PaymentCallbackRequest;
import com.yoteh.api.dto.request.RefundPaymentRequest;
import com.yoteh.api.dto.response.PaymentResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import com.yoteh.api.entity.Order;
import com.yoteh.api.entity.Payment;
import com.yoteh.api.entity.enums.OrderStatus;
import com.yoteh.api.entity.enums.PaymentMethod;
import com.yoteh.api.entity.enums.PaymentProvider;
import com.yoteh.api.entity.enums.PaymentStatus;
import com.yoteh.api.exception.BadRequestException;
import com.yoteh.api.exception.ResourceNotFoundException;
import com.yoteh.api.exception.UnauthorizedException;
import com.yoteh.api.mapper.PaymentMapper;
import com.yoteh.api.repository.OrderRepository;
import com.yoteh.api.repository.PaymentRepository;
import com.yoteh.api.service.PaymentService;
import com.yoteh.api.service.payment.MobileMoneyProvider;
import com.yoteh.api.service.payment.MtnMoneyProviderImpl;
import com.yoteh.api.service.payment.OrangeMoneyProviderImpl;
import com.yoteh.api.util.Constants;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;
    private final OrangeMoneyProviderImpl orangeMoneyProvider;
    private final MtnMoneyProviderImpl mtnMoneyProvider;
    private final ObjectMapper objectMapper;

    private static final int MOBILE_MONEY_EXPIRY_MINUTES = 30;
    private static final int CARD_EXPIRY_HOURS = 24;
    private static final Random RANDOM = new Random();

    // ─── Utilitaires privés ──────────────────────────────────────────────────

    private String generatePaymentReference() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.format("%04d", RANDOM.nextInt(10000));
        return "YTH-PAY-" + timestamp + "-" + random;
    }

    private MobileMoneyProvider resolveProvider(PaymentProvider provider) {
        if (provider == null) {
            throw new BadRequestException("Le provider Mobile Money est obligatoire");
        }
        return switch (provider) {
            case ORANGE_MONEY -> orangeMoneyProvider;
            case MTN_MONEY -> mtnMoneyProvider;
            default -> throw new BadRequestException("Provider non supporté : " + provider);
        };
    }

    private void markOrderPaid(Order order) {
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
        log.info("[Payment] Commande {} marquée PAID", order.getOrderNumber());
    }

    private void checkExpiration(Payment payment) {
        if (payment.getExpiresAt() != null
                && payment.getExpiresAt().isBefore(LocalDateTime.now())
                && (payment.getStatus() == PaymentStatus.PENDING
                        || payment.getStatus() == PaymentStatus.PROCESSING)) {
            payment.setStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(payment);
            throw new BadRequestException("Ce paiement a expiré le " + payment.getExpiresAt());
        }
    }

    // ─── Initiation du paiement ──────────────────────────────────────────────

    @Override
    @Transactional
    public PaymentResponse initiatePayment(InitPaymentRequest request, UUID userId) {
        // 1. Vérifier que la commande existe et appartient à l'utilisateur
        Order order =
                orderRepository
                        .findById(request.getOrderId())
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Commande", "id", request.getOrderId()));

        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Accès refusé à cette commande");
        }

        // 2. Valider le statut de la commande : seules les commandes PENDING peuvent être payées
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException(
                    "Impossible de payer une commande avec le statut : " + order.getStatus());
        }

        // 3. Vérifier qu'aucun paiement COMPLETED n'existe déjà pour cette commande
        if (paymentRepository.existsByOrderIdAndStatus(order.getId(), PaymentStatus.COMPLETED)) {
            throw new BadRequestException("Cette commande a déjà été réglée");
        }

        // 4. Construire l'entité Payment avec les champs exacts de l'entité
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setUser(order.getUser());
        payment.setPaymentReference(generatePaymentReference());
        payment.setMethod(request.getMethod());
        payment.setProvider(request.getProvider());
        payment.setAmount(order.getTotal());
        payment.setCurrency(
                (request.getCurrency() != null && !request.getCurrency().isBlank())
                        ? request.getCurrency()
                        : order.getCurrency());
        payment.setStatus(PaymentStatus.PENDING);

        // 5. Logique par méthode de paiement
        switch (request.getMethod()) {
            case MOBILE_MONEY -> {
                if (request.getPhoneNumber() == null || request.getPhoneNumber().isBlank()) {
                    throw new BadRequestException(
                            "Le numéro de téléphone est obligatoire pour le Mobile Money");
                }
                payment.setPhoneNumber(request.getPhoneNumber());
                MobileMoneyProvider provider = resolveProvider(request.getProvider());
                provider.initiatePayment(
                        payment); // renseigne externalId, externalUrl, providerResponse
                payment.setStatus(PaymentStatus.PROCESSING);
                payment.setExpiresAt(LocalDateTime.now().plusMinutes(MOBILE_MONEY_EXPIRY_MINUTES));
            }
            case CASH_ON_DELIVERY -> {
                payment.setProvider(null);
                payment.setStatus(PaymentStatus.PENDING);
                // Pas d'expiration : confirmé lors de la livraison
            }
            case CARD -> {
                // TODO : Intégration Stripe ou CinetPay pour les cartes bancaires
                log.info(
                        "[Payment] Paiement par carte initié (stub) - ref: {}",
                        payment.getPaymentReference());
                payment.setStatus(PaymentStatus.PENDING);
                payment.setExpiresAt(LocalDateTime.now().plusHours(CARD_EXPIRY_HOURS));
            }
        }

        Payment saved = paymentRepository.save(payment);
        log.info(
                "[Payment] Créé - ref: {}, méthode: {}, montant: {} {}",
                saved.getPaymentReference(),
                saved.getMethod(),
                saved.getAmount(),
                saved.getCurrency());
        return paymentMapper.toResponse(saved);
    }

    // ─── Confirmation de paiement ─────────────────────────────────────────────

    @Override
    @Transactional
    public PaymentResponse confirmPayment(String paymentReference, PaymentCallbackRequest request) {
        Payment payment =
                paymentRepository
                        .findByPaymentReference(paymentReference)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Paiement", "référence", paymentReference));

        // Idempotence : si déjà confirmé, retourner sans modifier
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            log.info("[Payment] Déjà confirmé - ref: {}", paymentReference);
            return paymentMapper.toResponse(payment);
        }

        if (payment.getStatus() == PaymentStatus.FAILED
                || payment.getStatus() == PaymentStatus.EXPIRED
                || payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new BadRequestException(
                    "Ce paiement ne peut pas être confirmé : statut " + payment.getStatus());
        }

        checkExpiration(payment);

        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(LocalDateTime.now());
        if (request.getTransactionId() != null && !request.getTransactionId().isBlank()) {
            payment.setExternalId(request.getTransactionId());
        }

        markOrderPaid(payment.getOrder());

        Payment saved = paymentRepository.save(payment);
        log.info(
                "[Payment] Confirmé - ref: {}, commande: {}",
                saved.getPaymentReference(),
                saved.getOrder().getOrderNumber());
        return paymentMapper.toResponse(saved);
    }

    // ─── Webhook provider ────────────────────────────────────────────────────

    @Override
    @Transactional
    public void handleWebhook(String provider, String payload, String signature) {
        log.info("[Webhook] Réception - provider: {}", provider);

        try {
            JsonNode node = objectMapper.readTree(payload);

            // Extraire l'externalId selon les formats connus des providers
            String externalId = extractExternalId(node);
            if (externalId == null) {
                log.warn("[Webhook] ExternalId introuvable dans le payload");
                return;
            }

            Payment payment = paymentRepository.findByExternalId(externalId).orElse(null);
            if (payment == null) {
                log.warn("[Webhook] Aucun paiement trouvé pour externalId: {}", externalId);
                return;
            }

            // Sauvegarder la réponse brute du provider
            payment.setProviderResponse(payload);

            String statusStr = node.has("status") ? node.get("status").asText().toUpperCase() : "";

            if ("SUCCESS".equals(statusStr)
                    || "SUCCESSFUL".equals(statusStr)
                    || "COMPLETED".equals(statusStr)) {
                // Inliner la logique de confirmation pour éviter les problèmes de self-invocation
                // AOP
                if (payment.getStatus() != PaymentStatus.COMPLETED) {
                    payment.setStatus(PaymentStatus.COMPLETED);
                    payment.setPaidAt(LocalDateTime.now());
                    markOrderPaid(payment.getOrder());
                }
            } else if ("FAILED".equals(statusStr) || "FAILURE".equals(statusStr)) {
                payment.setStatus(PaymentStatus.FAILED);
                if (node.has("error_code")) {
                    payment.setErrorCode(node.get("error_code").asText());
                }
                if (node.has("error_message")) {
                    payment.setErrorMessage(node.get("error_message").asText());
                }
                log.info("[Webhook] Paiement échoué - ref: {}", payment.getPaymentReference());
            } else {
                log.info("[Webhook] Statut non reconnu '{}' - ignoré", statusStr);
            }

            paymentRepository.save(payment);

        } catch (Exception e) {
            log.error("[Webhook] Erreur lors du traitement : {}", e.getMessage(), e);
        }
    }

    private String extractExternalId(JsonNode node) {
        if (node.has("externalId") && !node.get("externalId").isNull()) {
            return node.get("externalId").asText();
        }
        if (node.has("transaction_id") && !node.get("transaction_id").isNull()) {
            return node.get("transaction_id").asText();
        }
        if (node.has("id") && !node.get("id").isNull()) {
            return node.get("id").asText();
        }
        return null;
    }

    // ─── Vérification de statut (polling) ────────────────────────────────────

    @Override
    @Transactional
    public PaymentResponse verifyPayment(String paymentReference, UUID userId) {
        Payment payment =
                paymentRepository
                        .findByPaymentReference(paymentReference)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Paiement", "référence", paymentReference));

        if (!payment.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Accès refusé à ce paiement");
        }

        // Vérifier l'expiration
        if ((payment.getStatus() == PaymentStatus.PENDING
                        || payment.getStatus() == PaymentStatus.PROCESSING)
                && payment.getExpiresAt() != null
                && payment.getExpiresAt().isBefore(LocalDateTime.now())) {
            payment.setStatus(PaymentStatus.EXPIRED);
            payment = paymentRepository.save(payment);
            return paymentMapper.toResponse(payment);
        }

        // Polling auprès du provider si le paiement est en cours et a un externalId
        if (payment.getStatus() == PaymentStatus.PROCESSING
                && payment.getExternalId() != null
                && payment.getProvider() != null) {
            try {
                MobileMoneyProvider provider = resolveProvider(payment.getProvider());
                PaymentStatus newStatus = provider.checkPaymentStatus(payment.getExternalId());
                if (newStatus == PaymentStatus.COMPLETED) {
                    payment.setStatus(PaymentStatus.COMPLETED);
                    payment.setPaidAt(LocalDateTime.now());
                    markOrderPaid(payment.getOrder());
                    payment = paymentRepository.save(payment);
                }
            } catch (Exception e) {
                log.warn(
                        "[Payment] Impossible de vérifier le statut provider : {}", e.getMessage());
            }
        }

        return paymentMapper.toResponse(payment);
    }

    // ─── Historique utilisateur ───────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PaymentResponse> getPaymentHistory(UUID userId, int page, int size) {
        int safeSize = Math.min(size, Constants.MAX_PAGE_SIZE);
        Pageable pageable =
                PageRequest.of(
                        page, safeSize, Sort.by(Sort.Direction.DESC, Constants.DEFAULT_SORT_BY));
        Page<Payment> pageResult = paymentRepository.findByUserId(userId, pageable);
        return PagedResponse.of(
                pageResult.getContent().stream().map(paymentMapper::toResponse).toList(),
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast());
    }

    // ─── Détail paiement utilisateur ──────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentDetail(String paymentReference, UUID userId) {
        Payment payment =
                paymentRepository
                        .findByPaymentReference(paymentReference)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Paiement", "référence", paymentReference));
        if (!payment.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Accès refusé à ce paiement");
        }
        return paymentMapper.toResponse(payment);
    }

    // ─── Admin : liste filtrée ─────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PaymentResponse> getAllPayments(
            int page, int size, String status, String provider, String sortDir) {

        int safeSize = Math.min(size, Constants.MAX_PAGE_SIZE);
        Sort.Direction direction =
                "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable =
                PageRequest.of(page, safeSize, Sort.by(direction, Constants.DEFAULT_SORT_BY));

        PaymentStatus statusEnum = parseEnum(status, PaymentStatus.class, "statut");
        PaymentProvider providerEnum = parseEnum(provider, PaymentProvider.class, "provider");

        Page<Payment> pageResult =
                paymentRepository.findAllWithFilters(statusEnum, providerEnum, pageable);
        return PagedResponse.of(
                pageResult.getContent().stream().map(paymentMapper::toResponse).toList(),
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast());
    }

    private <E extends Enum<E>> E parseEnum(String value, Class<E> enumClass, String label) {
        if (value == null || value.isBlank()) return null;
        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Valeur de " + label + " invalide : " + value);
        }
    }

    // ─── Admin : détail ───────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse adminGetPayment(String paymentReference) {
        Payment payment =
                paymentRepository
                        .findByPaymentReference(paymentReference)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Paiement", "référence", paymentReference));
        return paymentMapper.toResponse(payment);
    }

    // ─── Admin : remboursement ────────────────────────────────────────────────

    @Override
    @Transactional
    public PaymentResponse refundPayment(String paymentReference, RefundPaymentRequest request) {
        Payment payment =
                paymentRepository
                        .findByPaymentReference(paymentReference)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Paiement", "référence", paymentReference));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new BadRequestException(
                    "Impossible de rembourser un paiement avec le statut : " + payment.getStatus());
        }

        BigDecimal refundAmount =
                (request.getAmount() != null) ? request.getAmount() : payment.getAmount();

        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new BadRequestException(
                    "Le montant du remboursement ("
                            + refundAmount
                            + ") dépasse le montant du paiement ("
                            + payment.getAmount()
                            + ")");
        }

        // Tentative de remboursement auprès du provider Mobile Money
        if (payment.getMethod() == PaymentMethod.MOBILE_MONEY && payment.getProvider() != null) {
            try {
                MobileMoneyProvider provider = resolveProvider(payment.getProvider());
                boolean providerRefunded =
                        provider.processRefund(payment, refundAmount, request.getReason());
                log.info(
                        "[Payment] Réponse remboursement provider {} : {}",
                        payment.getProvider(),
                        providerRefunded);
            } catch (Exception e) {
                log.warn(
                        "[Payment] Remboursement provider échoué (on poursuit quand même) : {}",
                        e.getMessage());
            }
        }

        // Mettre à jour l'entité Payment
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAt(LocalDateTime.now());
        payment.setRefundedAmount(refundAmount);
        payment.setRefundReason(request.getReason());

        // Si remboursement total : mettre la commande en REFUNDED
        if (refundAmount.compareTo(payment.getAmount()) == 0) {
            Order order = payment.getOrder();
            order.setStatus(OrderStatus.REFUNDED);
            orderRepository.save(order);
            log.info("[Payment] Commande {} marquée REFUNDED", order.getOrderNumber());
        }

        Payment saved = paymentRepository.save(payment);
        log.info(
                "[Payment] Remboursement enregistré - ref: {}, montant: {}",
                paymentReference,
                refundAmount);
        return paymentMapper.toResponse(saved);
    }
}
