package com.yoteh.api.controller;

import com.yoteh.api.dto.request.ShippingCalculateRequest;
import com.yoteh.api.dto.request.ShippingZoneRequest;
import com.yoteh.api.dto.response.ShippingCalculateResponse;
import com.yoteh.api.dto.response.ShippingZoneResponse;
import com.yoteh.api.dto.response.common.ApiResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import com.yoteh.api.service.ShippingService;
import com.yoteh.api.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
@Tag(name = "Shipping", description = "Gestion des zones et frais de livraison")
public class ShippingController {

    private final ShippingService shippingService;

    // ═══════════════════════════════════════════════════════════
    // ENDPOINTS PUBLICS
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/zones")
    @Operation(summary = "Lister toutes les zones de livraison actives")
    public ResponseEntity<ApiResponse<List<ShippingZoneResponse>>> getActiveZones(
            @RequestParam(required = false) String city) {

        List<ShippingZoneResponse> zones =
                (city != null && !city.isBlank())
                        ? shippingService.findZonesByCity(city)
                        : shippingService.getActiveZones();

        return ResponseEntity.ok(ApiResponse.success(zones, "Zones de livraison récupérées"));
    }

    @GetMapping("/zones/{id}")
    @Operation(summary = "Détail d'une zone de livraison active")
    public ResponseEntity<ApiResponse<ShippingZoneResponse>> getZoneById(@PathVariable UUID id) {

        ShippingZoneResponse zone = shippingService.getActiveZoneById(id);
        return ResponseEntity.ok(ApiResponse.success(zone, "Zone de livraison récupérée"));
    }

    @PostMapping("/calculate")
    @Operation(summary = "Calculer les frais de livraison (zoneId, orderTotal, weightKg)")
    public ResponseEntity<ApiResponse<ShippingCalculateResponse>> calculateShipping(
            @Valid @RequestBody ShippingCalculateRequest request) {

        ShippingCalculateResponse result = shippingService.calculateShipping(request);
        return ResponseEntity.ok(ApiResponse.success(result, "Frais de livraison calculés"));
    }

    // ═══════════════════════════════════════════════════════════
    // ENDPOINTS ADMIN
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/admin/zones")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Lister toutes les zones (admin, paginé)")
    public ResponseEntity<ApiResponse<PagedResponse<ShippingZoneResponse>>> getAllZones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "sortOrder") String sortBy,
            @RequestParam(defaultValue = Constants.DEFAULT_SORT_DIR) String sortDir,
            @RequestParam(required = false) String search) {

        PagedResponse<ShippingZoneResponse> result =
                shippingService.getAllZones(page, size, sortBy, sortDir, search);
        return ResponseEntity.ok(ApiResponse.success(result, "Toutes les zones récupérées"));
    }

    @GetMapping("/admin/zones/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Détail d'une zone (admin, y compris inactives)")
    public ResponseEntity<ApiResponse<ShippingZoneResponse>> getAdminZoneById(
            @PathVariable UUID id) {

        ShippingZoneResponse zone = shippingService.getZoneById(id);
        return ResponseEntity.ok(ApiResponse.success(zone, "Zone récupérée"));
    }

    @PostMapping("/admin/zones")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer une nouvelle zone de livraison")
    public ResponseEntity<ApiResponse<ShippingZoneResponse>> createZone(
            @Valid @RequestBody ShippingZoneRequest request) {

        ShippingZoneResponse created = shippingService.createZone(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Zone de livraison créée"));
    }

    @PutMapping("/admin/zones/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour une zone de livraison")
    public ResponseEntity<ApiResponse<ShippingZoneResponse>> updateZone(
            @PathVariable UUID id, @Valid @RequestBody ShippingZoneRequest request) {

        ShippingZoneResponse updated = shippingService.updateZone(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Zone de livraison mise à jour"));
    }

    @PatchMapping("/admin/zones/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Activer / désactiver une zone de livraison")
    public ResponseEntity<ApiResponse<ShippingZoneResponse>> toggleZoneStatus(
            @PathVariable UUID id) {

        ShippingZoneResponse updated = shippingService.toggleZoneStatus(id);
        String msg =
                Boolean.TRUE.equals(updated.getIsActive()) ? "Zone activée" : "Zone désactivée";
        return ResponseEntity.ok(ApiResponse.success(updated, msg));
    }

    @DeleteMapping("/admin/zones/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer une zone de livraison (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteZone(@PathVariable UUID id) {
        shippingService.deleteZone(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Zone de livraison supprimée"));
    }
}
