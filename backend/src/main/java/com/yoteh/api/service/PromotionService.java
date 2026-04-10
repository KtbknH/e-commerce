package com.yoteh.api.service;

import com.yoteh.api.dto.request.ApplyPromoRequest;
import com.yoteh.api.dto.request.PromotionRequest;
import com.yoteh.api.dto.response.ApplyPromoResponse;
import com.yoteh.api.dto.response.PromotionResponse;
import com.yoteh.api.dto.response.PromotionStatsResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import java.util.List;
import java.util.UUID;

public interface PromotionService {

    /** Crée une nouvelle promotion (admin). */
    PromotionResponse createPromotion(PromotionRequest request);

    /** Met à jour une promotion existante (admin). */
    PromotionResponse updatePromotion(UUID id, PromotionRequest request);

    /** Soft-delete d'une promotion (admin). */
    void deletePromotion(UUID id);

    /** Récupère le détail d'une promotion par son ID (admin). */
    PromotionResponse getPromotionById(UUID id);

    /** Liste paginée avec filtres (admin). */
    PagedResponse<PromotionResponse> getAllPromotions(
            int page, int size, Boolean isActive, Boolean isFlashSale, String search);

    /** Liste des ventes flash en cours (publique). */
    List<PromotionResponse> getActiveFlashSales();

    /**
     * Valide un code promo contre un montant et retourne la remise calculée. Incrémente usedCount
     * lors d'une application confirmée.
     */
    ApplyPromoResponse applyPromoCode(ApplyPromoRequest request);

    /** Statistiques d'utilisation d'une promotion (admin). */
    PromotionStatsResponse getPromotionStats(UUID id);
}
