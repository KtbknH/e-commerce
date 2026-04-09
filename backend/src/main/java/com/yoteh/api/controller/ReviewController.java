package com.yoteh.api.controller;

import com.yoteh.api.dto.request.AdminReviewRequest;
import com.yoteh.api.dto.request.ReviewRequest;
import com.yoteh.api.dto.response.ReviewResponse;
import com.yoteh.api.dto.response.common.ApiResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import com.yoteh.api.security.CustomUserDetails;
import com.yoteh.api.service.ReviewService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // ══════════════════════════════════════════════════════════════
    //  ENDPOINTS PUBLICS — liés à un produit
    // ══════════════════════════════════════════════════════════════

    // GET /api/v1/products/{productId}/reviews
    // Avis approuvés d'un produit (public, pas d'auth)
    @GetMapping("/api/v1/products/{productId}/reviews")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> getProductReviews(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<ReviewResponse> reviews =
                reviewService.getProductReviews(productId, page, size);
        return ResponseEntity.ok(ApiResponse.success(reviews, "Avis du produit"));
    }

    // POST /api/v1/products/{productId}/reviews
    // Soumettre un avis (authentifié)
    @PostMapping("/api/v1/products/{productId}/reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @PathVariable UUID productId,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ReviewResponse review = reviewService.createReview(userDetails.getId(), productId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(review, "Avis soumis — en attente de modération"));
    }

    // ══════════════════════════════════════════════════════════════
    //  ENDPOINTS AUTHENTIFIÉS — gestion de ses propres avis
    // ══════════════════════════════════════════════════════════════

    // GET /api/v1/reviews/my
    // Mes avis
    @GetMapping("/api/v1/reviews/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> getMyReviews(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<ReviewResponse> reviews =
                reviewService.getUserReviews(userDetails.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(reviews, "Mes avis"));
    }

    // PUT /api/v1/reviews/{reviewId}
    // Modifier un avis (propriétaire uniquement)
    @PutMapping("/api/v1/reviews/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ReviewResponse review = reviewService.updateReview(userDetails.getId(), reviewId, request);
        return ResponseEntity.ok(ApiResponse.success(review, "Avis modifié"));
    }

    // DELETE /api/v1/reviews/{reviewId}
    // Supprimer son avis (soft delete)
    @DeleteMapping("/api/v1/reviews/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable UUID reviewId, @AuthenticationPrincipal CustomUserDetails userDetails) {

        reviewService.deleteReview(userDetails.getId(), reviewId);
        return ResponseEntity.ok(ApiResponse.success(null, "Avis supprimé"));
    }

    // ══════════════════════════════════════════════════════════════
    //  ENDPOINTS ADMIN — modération
    // ══════════════════════════════════════════════════════════════

    // GET /api/v1/admin/reviews
    // Liste tous les avis avec filtre d'approbation (admin)
    @GetMapping("/api/v1/admin/reviews")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean approved) {

        PagedResponse<ReviewResponse> reviews = reviewService.getAllReviews(page, size, approved);
        return ResponseEntity.ok(ApiResponse.success(reviews, "Tous les avis"));
    }

    // PATCH /api/v1/admin/reviews/{reviewId}/moderate
    // Approuver ou rejeter un avis avec réponse optionnelle
    @PatchMapping("/api/v1/admin/reviews/{reviewId}/moderate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ReviewResponse>> moderateReview(
            @PathVariable UUID reviewId, @Valid @RequestBody AdminReviewRequest request) {

        ReviewResponse review = reviewService.moderateReview(reviewId, request);
        String msg = Boolean.TRUE.equals(request.getApproved()) ? "Avis approuvé" : "Avis rejeté";
        return ResponseEntity.ok(ApiResponse.success(review, msg));
    }

    // DELETE /api/v1/admin/reviews/{reviewId}
    // Suppression admin (soft delete)
    @DeleteMapping("/api/v1/admin/reviews/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> adminDeleteReview(@PathVariable UUID reviewId) {

        reviewService.adminDeleteReview(reviewId);
        return ResponseEntity.ok(ApiResponse.success(null, "Avis supprimé par l'admin"));
    }
}
