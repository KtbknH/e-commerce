package com.yoteh.api.controller;

import com.yoteh.api.dto.response.WishlistItemResponse;
import com.yoteh.api.dto.response.common.ApiResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import com.yoteh.api.security.CustomUserDetails;
import com.yoteh.api.service.WishlistService;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    // ─────────────────────────────────────────────────────────────
    //  GET /api/v1/wishlist
    //  Liste paginée des articles en wishlist de l'utilisateur connecté
    // ─────────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PagedResponse<WishlistItemResponse>>> getMyWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PagedResponse<WishlistItemResponse> wishlist =
                wishlistService.getWishlist(userDetails.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(wishlist, "Wishlist récupérée"));
    }

    // ─────────────────────────────────────────────────────────────
    //  POST /api/v1/wishlist/{productId}
    //  Ajouter un produit à la wishlist
    // ─────────────────────────────────────────────────────────────

    @PostMapping("/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WishlistItemResponse>> addToWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable UUID productId) {

        WishlistItemResponse item = wishlistService.addToWishlist(userDetails.getId(), productId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(item, "Produit ajouté à la wishlist"));
    }

    // ─────────────────────────────────────────────────────────────
    //  DELETE /api/v1/wishlist/{productId}
    //  Retirer un produit de la wishlist
    // ─────────────────────────────────────────────────────────────

    @DeleteMapping("/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable UUID productId) {

        wishlistService.removeFromWishlist(userDetails.getId(), productId);
        return ResponseEntity.ok(ApiResponse.success(null, "Produit retiré de la wishlist"));
    }

    // ─────────────────────────────────────────────────────────────
    //  GET /api/v1/wishlist/check/{productId}
    //  Vérifier si un produit est en wishlist
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/check/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable UUID productId) {

        boolean inWishlist = wishlistService.isInWishlist(userDetails.getId(), productId);
        long count = wishlistService.getWishlistCount(userDetails.getId());

        Map<String, Object> result =
                Map.of(
                        "inWishlist", inWishlist,
                        "wishlistCount", count);
        return ResponseEntity.ok(ApiResponse.success(result, "Statut wishlist"));
    }
}
