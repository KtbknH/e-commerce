package com.yoteh.api.controller;

import com.yoteh.api.dto.request.ApplyPromoRequest;
import com.yoteh.api.dto.request.PromotionRequest;
import com.yoteh.api.dto.response.ApplyPromoResponse;
import com.yoteh.api.dto.response.PromotionResponse;
import com.yoteh.api.dto.response.PromotionStatsResponse;
import com.yoteh.api.dto.response.common.ApiResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import com.yoteh.api.security.CustomUserDetails;
import com.yoteh.api.service.PromotionService;
import com.yoteh.api.util.Constants;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour la gestion des promotions.
 *
 * <p>Endpoints publics (sans authentification) : - GET /api/v1/promotions/flash-sales → ventes
 * flash actives - POST /api/v1/promotions/apply → validation et calcul d'un code promo
 *
 * <p>Endpoints admin (ADMIN ou MANAGER requis) : - POST /api/v1/promotions → créer - GET
 * /api/v1/promotions → liste paginée avec filtres - GET /api/v1/promotions/{id} → détail - PUT
 * /api/v1/promotions/{id} → modifier - DELETE /api/v1/promotions/{id} → supprimer (ADMIN
 * uniquement) - GET /api/v1/promotions/{id}/stats → statistiques d'utilisation
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    // ═══════════════════════════════════════════════════════════════════════════
    // ENDPOINTS PUBLICS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/promotions/flash-sales
     *
     * <p>Retourne les ventes flash actuellement actives (isFlashSale=true, dans leur plage de
     * dates). Utilisé par la page d'accueil et la bannière de promotions.
     */
    @GetMapping("/flash-sales")
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getActiveFlashSales() {
        List<PromotionResponse> flashSales = promotionService.getActiveFlashSales();
        return ResponseEntity.ok(
                ApiResponse.success(flashSales, "Ventes flash actives récupérées"));
    }

    /**
     * POST /api/v1/promotions/apply
     *
     * <p>Valide un code promo contre un montant de commande et retourne la remise calculée.
     * Incrémente le compteur usedCount → à appeler uniquement lors d'une commande effective.
     *
     * <p>Body : { "code": "PROMO10", "orderAmount": 25000 }
     */
    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<ApplyPromoResponse>> applyPromoCode(
            @Valid @RequestBody ApplyPromoRequest request) {
        ApplyPromoResponse response = promotionService.applyPromoCode(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Code promo appliqué avec succès"));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ENDPOINTS ADMIN
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * POST /api/v1/promotions
     *
     * <p>Crée une nouvelle promotion. Le code est normalisé en majuscules.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<PromotionResponse>> createPromotion(
            @Valid @RequestBody PromotionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info(
                "Admin '{}' crée une promotion : {}", userDetails.getUsername(), request.getCode());
        PromotionResponse response = promotionService.createPromotion(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Promotion créée avec succès"));
    }

    /**
     * GET /api/v1/promotions
     *
     * <p>Liste paginée avec filtres optionnels. Triée par date de création (desc).
     *
     * @param isActive filtre sur l'état actif/inactif (null = tous)
     * @param isFlashSale filtre sur les ventes flash (null = tous)
     * @param search recherche sur le code ou le nom
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<PagedResponse<PromotionResponse>>> getAllPromotions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean isFlashSale,
            @RequestParam(required = false) String search) {
        size = Math.min(size, Constants.MAX_PAGE_SIZE);
        PagedResponse<PromotionResponse> response =
                promotionService.getAllPromotions(page, size, isActive, isFlashSale, search);
        return ResponseEntity.ok(ApiResponse.success(response, "Promotions récupérées"));
    }

    /**
     * GET /api/v1/promotions/{id}
     *
     * <p>Détail complet d'une promotion par son UUID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<PromotionResponse>> getPromotionById(@PathVariable UUID id) {
        PromotionResponse response = promotionService.getPromotionById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Promotion récupérée"));
    }

    /**
     * PUT /api/v1/promotions/{id}
     *
     * <p>Mise à jour complète d'une promotion.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<PromotionResponse>> updatePromotion(
            @PathVariable UUID id,
            @Valid @RequestBody PromotionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Admin '{}' modifie la promotion : {}", userDetails.getUsername(), id);
        PromotionResponse response = promotionService.updatePromotion(id, request);
        return ResponseEntity.ok(
                ApiResponse.success(response, "Promotion mise à jour avec succès"));
    }

    /**
     * DELETE /api/v1/promotions/{id}
     *
     * <p>Soft-delete — réservé aux ADMIN uniquement (pas MANAGER).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePromotion(
            @PathVariable UUID id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Admin '{}' supprime la promotion : {}", userDetails.getUsername(), id);
        promotionService.deletePromotion(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Promotion supprimée avec succès"));
    }

    /**
     * GET /api/v1/promotions/{id}/stats
     *
     * <p>Statistiques d'utilisation : usedCount, maxUses, taux d'utilisation.
     */
    @GetMapping("/{id}/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<PromotionStatsResponse>> getPromotionStats(
            @PathVariable UUID id) {
        PromotionStatsResponse stats = promotionService.getPromotionStats(id);
        return ResponseEntity.ok(ApiResponse.success(stats, "Statistiques récupérées"));
    }
}
