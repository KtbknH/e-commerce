package com.yoteh.api.service;

import com.yoteh.api.dto.request.ShippingCalculateRequest;
import com.yoteh.api.dto.request.ShippingZoneRequest;
import com.yoteh.api.dto.response.ShippingCalculateResponse;
import com.yoteh.api.dto.response.ShippingZoneResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import java.util.List;
import java.util.UUID;

public interface ShippingService {

    // ── Publiques ─────────────────────────────────────────────────────────────

    /** Retourne toutes les zones actives, triées par sortOrder. */
    List<ShippingZoneResponse> getActiveZones();

    /** Retourne les zones actives correspondant à une ville donnée. */
    List<ShippingZoneResponse> findZonesByCity(String city);

    /** Retourne le détail d'une zone active (accessible sans auth). */
    ShippingZoneResponse getActiveZoneById(UUID id);

    /** Calcule les frais de livraison selon la zone, le montant et le poids. */
    ShippingCalculateResponse calculateShipping(ShippingCalculateRequest request);

    // ── Admin ─────────────────────────────────────────────────────────────────

    /** Liste paginée de toutes les zones (actives + inactives). */
    PagedResponse<ShippingZoneResponse> getAllZones(
            int page, int size, String sortBy, String sortDir, String search);

    /** Détail d'une zone (admin, y compris inactives). */
    ShippingZoneResponse getZoneById(UUID id);

    /** Créer une nouvelle zone. */
    ShippingZoneResponse createZone(ShippingZoneRequest request);

    /** Mettre à jour une zone existante. */
    ShippingZoneResponse updateZone(UUID id, ShippingZoneRequest request);

    /** Activer/désactiver une zone (toggle). */
    ShippingZoneResponse toggleZoneStatus(UUID id);

    /** Soft delete d'une zone. */
    void deleteZone(UUID id);
}
