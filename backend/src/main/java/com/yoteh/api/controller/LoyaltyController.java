package com.yoteh.api.controller;

import com.yoteh.api.dto.request.AdjustPointsRequest;
import com.yoteh.api.dto.response.LoyaltyBalanceResponse;
import com.yoteh.api.dto.response.LoyaltyTransactionResponse;
import com.yoteh.api.dto.response.common.ApiResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import com.yoteh.api.security.CustomUserDetails;
import com.yoteh.api.service.LoyaltyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Loyalty", description = "Programme de fidélité")
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    // ═══════════════════════════════════════════════════════════
    // CLIENT : MON PROGRAMME DE FIDÉLITÉ
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/loyalty/balance")
    @Operation(summary = "Obtenir mon solde de points et niveau de fidélité")
    public ResponseEntity<ApiResponse<LoyaltyBalanceResponse>> getMyBalance(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        LoyaltyBalanceResponse response = loyaltyService.getMyBalance(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Solde de fidélité récupéré"));
    }

    @GetMapping("/loyalty/history")
    @Operation(summary = "Historique de mes transactions de fidélité")
    public ResponseEntity<ApiResponse<PagedResponse<LoyaltyTransactionResponse>>> getMyHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type) {
        PagedResponse<LoyaltyTransactionResponse> response =
                loyaltyService.getMyHistory(userDetails.getId(), page, size, type);
        return ResponseEntity.ok(ApiResponse.success(response, "Historique récupéré"));
    }

    // ═══════════════════════════════════════════════════════════
    // ADMIN : GESTION DU PROGRAMME DE FIDÉLITÉ
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/admin/loyalty/transactions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORDER_MANAGER')")
    @Operation(summary = "Lister toutes les transactions de fidélité (admin)")
    public ResponseEntity<ApiResponse<PagedResponse<LoyaltyTransactionResponse>>>
            getAllTransactions(
                    @RequestParam(defaultValue = "0") int page,
                    @RequestParam(defaultValue = "20") int size,
                    @RequestParam(required = false) String type) {
        PagedResponse<LoyaltyTransactionResponse> response =
                loyaltyService.getAllTransactions(page, size, type);
        return ResponseEntity.ok(ApiResponse.success(response, "Transactions récupérées"));
    }

    @GetMapping("/admin/loyalty/users/{userId}/balance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORDER_MANAGER')")
    @Operation(summary = "Voir le solde de fidélité d'un utilisateur (admin)")
    public ResponseEntity<ApiResponse<LoyaltyBalanceResponse>> getUserBalance(
            @PathVariable UUID userId) {
        LoyaltyBalanceResponse response = loyaltyService.getUserBalance(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Solde récupéré"));
    }

    @PostMapping("/admin/loyalty/users/{userId}/adjust")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORDER_MANAGER')")
    @Operation(summary = "Ajuster les points d'un utilisateur (admin)")
    public ResponseEntity<ApiResponse<LoyaltyTransactionResponse>> adjustPoints(
            @PathVariable UUID userId, @Valid @RequestBody AdjustPointsRequest request) {
        LoyaltyTransactionResponse response = loyaltyService.adjustPoints(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Points ajustés"));
    }
}
