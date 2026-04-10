package com.yoteh.api.service;

import com.yoteh.api.dto.request.AdminReviewRequest;
import com.yoteh.api.dto.request.ReviewRequest;
import com.yoteh.api.dto.response.ReviewResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import java.util.UUID;

public interface ReviewService {

    // ─── Client ────────────────────────────────────────────────
    PagedResponse<ReviewResponse> getProductReviews(UUID productId, int page, int size);

    PagedResponse<ReviewResponse> getUserReviews(UUID userId, int page, int size);

    ReviewResponse createReview(UUID userId, UUID productId, ReviewRequest request);

    ReviewResponse updateReview(UUID userId, UUID reviewId, ReviewRequest request);

    void deleteReview(UUID userId, UUID reviewId);

    // ─── Admin ─────────────────────────────────────────────────
    PagedResponse<ReviewResponse> getAllReviews(int page, int size, Boolean approved);

    ReviewResponse moderateReview(UUID reviewId, AdminReviewRequest request);

    void adminDeleteReview(UUID reviewId);
}
