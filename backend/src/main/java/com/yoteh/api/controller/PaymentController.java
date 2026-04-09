package com.yoteh.api.controller;

import com.yoteh.api.dto.request.InitPaymentRequest;
import com.yoteh.api.dto.request.PaymentCallbackRequest;
import com.yoteh.api.dto.request.RefundPaymentRequest;
import com.yoteh.api.dto.response.PaymentResponse;
import com.yoteh.api.dto.response.common.ApiResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import com.yoteh.api.security.CustomUserDetails;
import com.yoteh.api.service.PaymentService;
import com.yoteh.api.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Paiements", description = "Mobile Money (Orange, MTN), carte bancaire, à la livraison")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    // ─── Endpoints utilisateur ───────────────────────────────────────────────

    /**
     * Initier un paiement pour une commande PENDING. Retourne la référence de paiement et l'URL
     * externe si applicable.
     */
    @PostMapping("/payments/initiate")
    @Operation(summary = "Initier un paiement")
    public ResponseEntity<ApiResponse<PaymentResponse>> initiatePayment(
            @Valid @RequestBody InitPaymentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PaymentResponse response = paymentService.initiatePayment(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Paiement initié avec succès"));
    }

    /**
     * Confirmer un paiement après retour depuis la page provider (Orange Money, MTN, etc.).
     * Endpoint public pour permettre la redirection sans token.
     */
    @PostMapping("/payments/{paymentReference}/confirm")
    @Operation(summary = "Confirmer un paiement (retour page provider)")
    public ResponseEntity<ApiResponse<PaymentResponse>> confirmPayment(
            @PathVariable String paymentReference,
            @Valid @RequestBody PaymentCallbackRequest request) {
        PaymentResponse response = paymentService.confirmPayment(paymentReference, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Paiement confirmé"));
    }

    /**
     * Vérifier le statut actuel d'un paiement (polling). Si le paiement est PROCESSING, interroge
     * le provider.
     */
    @PostMapping("/payments/{paymentReference}/verify")
    @Operation(summary = "Vérifier le statut d'un paiement")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(
            @PathVariable String paymentReference,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PaymentResponse response =
                paymentService.verifyPayment(paymentReference, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Statut du paiement"));
    }

    @GetMapping("/payments/history")
    @Operation(summary = "Historique des paiements de l'utilisateur connecté")
    public ResponseEntity<ApiResponse<PagedResponse<PaymentResponse>>> getPaymentHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PagedResponse<PaymentResponse> response =
                paymentService.getPaymentHistory(userDetails.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(response, "Historique des paiements"));
    }

    @GetMapping("/payments/{paymentReference}")
    @Operation(summary = "Détail d'un paiement")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentDetail(
            @PathVariable String paymentReference,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PaymentResponse response =
                paymentService.getPaymentDetail(paymentReference, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Détail du paiement"));
    }

    // ─── Webhook provider (public - sécurisé par signature) ──────────────────

    /**
     * Endpoint webhook reçu par les providers (Orange Money, MTN, CinetPay). Public car appelé
     * directement par le serveur du provider sans token JWT. La signature dans X-Signature doit
     * être validée (TODO phase intégration réelle).
     */
    @PostMapping("/payments/callback/{provider}")
    @Operation(summary = "Webhook callback provider (Orange Money, MTN, etc.)")
    public ResponseEntity<ApiResponse<Void>> handleWebhook(
            @PathVariable String provider,
            @RequestBody String payload,
            @RequestHeader(value = "X-Signature", required = false) String signature) {
        paymentService.handleWebhook(provider, payload, signature);
        return ResponseEntity.ok(ApiResponse.success(null, "Webhook reçu"));
    }

    // ─── Endpoints Admin ──────────────────────────────────────────────────────

    @GetMapping("/admin/payments")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Lister tous les paiements avec filtres (admin)")
    public ResponseEntity<ApiResponse<PagedResponse<PaymentResponse>>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String provider,
            @RequestParam(defaultValue = Constants.DEFAULT_SORT_DIR) String sortDir) {
        PagedResponse<PaymentResponse> response =
                paymentService.getAllPayments(page, size, status, provider, sortDir);
        return ResponseEntity.ok(ApiResponse.success(response, "Liste des paiements"));
    }

    @GetMapping("/admin/payments/{paymentReference}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Détail d'un paiement (admin)")
    public ResponseEntity<ApiResponse<PaymentResponse>> adminGetPayment(
            @PathVariable String paymentReference) {
        PaymentResponse response = paymentService.adminGetPayment(paymentReference);
        return ResponseEntity.ok(ApiResponse.success(response, "Détail du paiement"));
    }

    @PostMapping("/admin/payments/{paymentReference}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Rembourser un paiement (admin)")
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(
            @PathVariable String paymentReference,
            @Valid @RequestBody RefundPaymentRequest request) {
        PaymentResponse response = paymentService.refundPayment(paymentReference, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Remboursement effectué"));
    }
}
