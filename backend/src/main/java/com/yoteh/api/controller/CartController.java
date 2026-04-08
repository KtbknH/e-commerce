package com.yoteh.api.controller;

import com.yoteh.api.dto.request.CartItemRequest;
import com.yoteh.api.dto.response.CartResponse;
import com.yoteh.api.dto.response.common.ApiResponse;
import com.yoteh.api.security.CustomUserDetails;
import com.yoteh.api.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Panier", description = "Gestion du panier")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Obtenir le panier de l'utilisateur connecté")
    public ResponseEntity<ApiResponse> getCart(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CartResponse cart = cartService.getCart(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(cart, "Panier récupéré"));
    }

    @PostMapping("/items")
    @Operation(summary = "Ajouter un article au panier")
    public ResponseEntity<ApiResponse> addItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CartItemRequest request) {
        CartResponse cart = cartService.addItem(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(cart, "Article ajouté au panier"));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Mettre à jour la quantité d'un article")
    public ResponseEntity<ApiResponse> updateItemQuantity(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID itemId,
            @RequestParam Integer quantity) {
        CartResponse cart = cartService.updateItemQuantity(userDetails.getId(), itemId, quantity);
        return ResponseEntity.ok(ApiResponse.success(cart, "Quantité mise à jour"));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Supprimer un article du panier")
    public ResponseEntity<ApiResponse> removeItem(
            @AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable UUID itemId) {
        CartResponse cart = cartService.removeItem(userDetails.getId(), itemId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Article supprimé du panier"));
    }

    @DeleteMapping
    @Operation(summary = "Vider le panier")
    public ResponseEntity<ApiResponse> clearCart(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        cartService.clearCart(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Panier vidé"));
    }
}
