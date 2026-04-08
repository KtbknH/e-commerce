package com.yoteh.api.controller;

import com.yoteh.api.dto.request.OrderRequest;
import com.yoteh.api.dto.response.OrderListResponse;
import com.yoteh.api.dto.response.OrderResponse;
import com.yoteh.api.dto.response.common.ApiResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import com.yoteh.api.entity.enums.OrderStatus;
import com.yoteh.api.security.CustomUserDetails;
import com.yoteh.api.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Commandes", description = "Gestion des commandes")
public class OrderController {

    private final OrderService orderService;

    // ─── Endpoints utilisateur ──────────────────────────────

    @PostMapping("/orders")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Créer une commande à partir du panier")
    public ResponseEntity<ApiResponse> createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OrderRequest request) {
        OrderResponse order = orderService.createOrder(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(order, "Commande créée avec succès"));
    }

    @GetMapping("/orders")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister mes commandes")
    public ResponseEntity<ApiResponse> getMyOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<OrderListResponse> orders =
                orderService.getUserOrders(userDetails.getId(), status, page, size);
        return ResponseEntity.ok(ApiResponse.success(orders, "Commandes récupérées"));
    }

    @GetMapping("/orders/{orderId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Détail d'une commande")
    public ResponseEntity<ApiResponse> getOrderDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable UUID orderId) {
        OrderResponse order = orderService.getOrderById(userDetails.getId(), orderId);
        return ResponseEntity.ok(ApiResponse.success(order, "Détail de la commande"));
    }

    @GetMapping("/orders/number/{orderNumber}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Détail d'une commande par numéro")
    public ResponseEntity<ApiResponse> getOrderByNumber(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String orderNumber) {
        OrderResponse order = orderService.getOrderByNumber(userDetails.getId(), orderNumber);
        return ResponseEntity.ok(ApiResponse.success(order, "Détail de la commande"));
    }

    @PatchMapping("/orders/{orderId}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Annuler une commande")
    public ResponseEntity<ApiResponse> cancelOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID orderId,
            @RequestParam(required = false) String reason) {
        OrderResponse order = orderService.cancelOrder(userDetails.getId(), orderId, reason);
        return ResponseEntity.ok(ApiResponse.success(order, "Commande annulée"));
    }

    // ─── Endpoints admin ───────────────────────────────────

    @GetMapping("/admin/orders")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "[Admin] Lister toutes les commandes avec filtres")
    public ResponseEntity<ApiResponse> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<OrderListResponse> orders =
                orderService.getAllOrders(status, search, from, to, page, size);
        return ResponseEntity.ok(ApiResponse.success(orders, "Commandes récupérées"));
    }

    @GetMapping("/admin/orders/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "[Admin] Détail d'une commande")
    public ResponseEntity<ApiResponse> getOrderDetailAdmin(@PathVariable UUID orderId) {
        OrderResponse order = orderService.getOrderDetailAdmin(orderId);
        return ResponseEntity.ok(ApiResponse.success(order, "Détail de la commande"));
    }

    @PatchMapping("/admin/orders/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "[Admin] Mettre à jour le statut d'une commande")
    public ResponseEntity<ApiResponse> updateOrderStatus(
            @PathVariable UUID orderId, @RequestParam OrderStatus status) {
        OrderResponse order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(ApiResponse.success(order, "Statut de la commande mis à jour"));
    }
}
