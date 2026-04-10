package com.yoteh.api.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private UUID id;

    // ─── Produit ───────────────────────────────────────────────
    private UUID productId;
    private String productName;
    private String productSlug;

    // ─── Utilisateur ───────────────────────────────────────────
    private UUID userId;
    private String userFullName;

    // ─── Contenu ───────────────────────────────────────────────
    private Integer rating;
    private String title;
    private String comment;
    private Boolean isApproved;
    private Boolean isVerifiedPurchase;
    private String adminResponse;
    private Integer helpfulCount;

    // ─── Dates ─────────────────────────────────────────────────
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
