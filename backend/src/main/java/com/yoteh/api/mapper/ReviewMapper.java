package com.yoteh.api.mapper;

import com.yoteh.api.dto.request.ReviewRequest;
import com.yoteh.api.dto.response.ReviewResponse;
import com.yoteh.api.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ReviewMapper {

    default ReviewResponse toResponse(Review review) {
        if (review == null) return null;

        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setRating(review.getRating());
        response.setTitle(review.getTitle());
        response.setComment(review.getComment());
        response.setIsApproved(review.getIsApproved());
        response.setIsVerifiedPurchase(review.getIsVerifiedPurchase());
        response.setAdminResponse(review.getAdminResponse());
        response.setHelpfulCount(review.getHelpfulCount());
        response.setCreatedAt(review.getCreatedAt());
        response.setUpdatedAt(review.getUpdatedAt());

        // Produit
        if (review.getProduct() != null) {
            response.setProductId(review.getProduct().getId());
            response.setProductName(review.getProduct().getName());
            response.setProductSlug(review.getProduct().getSlug());
        }

        // Utilisateur — getFullName() est défini sur l'entité User
        if (review.getUser() != null) {
            response.setUserId(review.getUser().getId());
            response.setUserFullName(review.getUser().getFullName());
        }

        return response;
    }

    default void updateEntityFromRequest(ReviewRequest request, Review review) {
        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setComment(request.getComment());
    }
}
