package com.yoteh.api.service.impl;

import com.yoteh.api.dto.request.AdminReviewRequest;
import com.yoteh.api.dto.request.ReviewRequest;
import com.yoteh.api.dto.response.ReviewResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import com.yoteh.api.entity.Product;
import com.yoteh.api.entity.Review;
import com.yoteh.api.entity.User;
import com.yoteh.api.entity.enums.OrderStatus;
import com.yoteh.api.exception.BusinessException;
import com.yoteh.api.exception.DuplicateResourceException;
import com.yoteh.api.exception.ResourceNotFoundException;
import com.yoteh.api.mapper.ReviewMapper;
import com.yoteh.api.repository.OrderRepository;
import com.yoteh.api.repository.ProductRepository;
import com.yoteh.api.repository.ReviewRepository;
import com.yoteh.api.repository.UserRepository;
import com.yoteh.api.service.ReviewService;
import com.yoteh.api.util.Constants;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ReviewMapper reviewMapper;

    // ─────────────────────────────────────────────────────────────
    //  AVIS D'UN PRODUIT (public — approuvés uniquement)
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> getProductReviews(UUID productId, int page, int size) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId.toString());
        }

        int safeSize = Math.min(size, Constants.MAX_PAGE_SIZE);
        Page<Review> reviewPage =
                reviewRepository.findApprovedByProductId(productId, PageRequest.of(page, safeSize));

        List<ReviewResponse> content =
                reviewPage.getContent().stream()
                        .map(reviewMapper::toResponse)
                        .collect(Collectors.toList());

        return PagedResponse.of(
                content,
                reviewPage.getNumber(),
                reviewPage.getSize(),
                reviewPage.getTotalElements(),
                reviewPage.getTotalPages(),
                reviewPage.isLast());
    }

    // ─────────────────────────────────────────────────────────────
    //  AVIS DE L'UTILISATEUR CONNECTÉ
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> getUserReviews(UUID userId, int page, int size) {
        int safeSize = Math.min(size, Constants.MAX_PAGE_SIZE);
        Page<Review> reviewPage =
                reviewRepository.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(
                        userId, PageRequest.of(page, safeSize));

        List<ReviewResponse> content =
                reviewPage.getContent().stream()
                        .map(reviewMapper::toResponse)
                        .collect(Collectors.toList());

        return PagedResponse.of(
                content,
                reviewPage.getNumber(),
                reviewPage.getSize(),
                reviewPage.getTotalElements(),
                reviewPage.getTotalPages(),
                reviewPage.isLast());
    }

    // ─────────────────────────────────────────────────────────────
    //  CRÉER UN AVIS
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ReviewResponse createReview(UUID userId, UUID productId, ReviewRequest request) {
        // Un seul avis par utilisateur par produit
        if (reviewRepository.existsByUserIdAndProductIdAndDeletedAtIsNull(userId, productId)) {
            throw new DuplicateResourceException("Review", "productId", productId.toString());
        }

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "User", "id", userId.toString()));

        Product product =
                productRepository
                        .findById(productId)
                        .filter(
                                p ->
                                        Boolean.TRUE.equals(p.getIsActive())
                                                && p.getDeletedAt() == null)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Product", "id", productId.toString()));

        // Détecter si l'utilisateur a bien acheté et reçu ce produit
        boolean isVerifiedPurchase =
                orderRepository.existsDeliveredOrderForProduct(
                        userId, productId, OrderStatus.DELIVERED);

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setComment(request.getComment());
        review.setIsVerifiedPurchase(isVerifiedPurchase);
        review.setIsApproved(false); // En attente de modération
        review.setHelpfulCount(0);

        Review saved = reviewRepository.save(review);
        log.info("Avis créé par l'utilisateur {} pour le produit {}", userId, productId);
        return reviewMapper.toResponse(saved);
    }

    // ─────────────────────────────────────────────────────────────
    //  MODIFIER UN AVIS (propriétaire uniquement)
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ReviewResponse updateReview(UUID userId, UUID reviewId, ReviewRequest request) {
        Review review =
                reviewRepository
                        .findById(reviewId)
                        .filter(r -> r.getDeletedAt() == null)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Review", "id", reviewId.toString()));

        if (!review.getUser().getId().equals(userId)) {
            throw new BusinessException("Vous ne pouvez modifier que vos propres avis");
        }

        reviewMapper.updateEntityFromRequest(request, review);
        review.setIsApproved(false); // Repasse en modération après modification

        Review saved = reviewRepository.save(review);
        log.debug("Avis {} modifié par l'utilisateur {}", reviewId, userId);
        return reviewMapper.toResponse(saved);
    }

    // ─────────────────────────────────────────────────────────────
    //  SUPPRIMER UN AVIS (propriétaire — soft delete)
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteReview(UUID userId, UUID reviewId) {
        Review review =
                reviewRepository
                        .findById(reviewId)
                        .filter(r -> r.getDeletedAt() == null)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Review", "id", reviewId.toString()));

        if (!review.getUser().getId().equals(userId)) {
            throw new BusinessException("Vous ne pouvez supprimer que vos propres avis");
        }

        review.setDeletedAt(LocalDateTime.now());
        reviewRepository.save(review);
        log.debug("Avis {} supprimé (soft) par l'utilisateur {}", reviewId, userId);
    }

    // ─────────────────────────────────────────────────────────────
    //  ADMIN — LISTE TOUS LES AVIS
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> getAllReviews(int page, int size, Boolean approved) {
        int safeSize = Math.min(size, Constants.MAX_PAGE_SIZE);
        Page<Review> reviewPage =
                reviewRepository.findAllFiltered(approved, PageRequest.of(page, safeSize));

        List<ReviewResponse> content =
                reviewPage.getContent().stream()
                        .map(reviewMapper::toResponse)
                        .collect(Collectors.toList());

        return PagedResponse.of(
                content,
                reviewPage.getNumber(),
                reviewPage.getSize(),
                reviewPage.getTotalElements(),
                reviewPage.getTotalPages(),
                reviewPage.isLast());
    }

    // ─────────────────────────────────────────────────────────────
    //  ADMIN — APPROUVER / REJETER UN AVIS
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ReviewResponse moderateReview(UUID reviewId, AdminReviewRequest request) {
        Review review =
                reviewRepository
                        .findById(reviewId)
                        .filter(r -> r.getDeletedAt() == null)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Review", "id", reviewId.toString()));

        review.setIsApproved(request.getApproved());
        if (request.getAdminResponse() != null) {
            review.setAdminResponse(request.getAdminResponse());
        }

        Review saved = reviewRepository.save(review);
        log.info(
                "Avis {} {} par un admin",
                reviewId,
                Boolean.TRUE.equals(request.getApproved()) ? "approuvé" : "rejeté");
        return reviewMapper.toResponse(saved);
    }

    // ─────────────────────────────────────────────────────────────
    //  ADMIN — SUPPRESSION DÉFINITIVE
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void adminDeleteReview(UUID reviewId) {
        Review review =
                reviewRepository
                        .findById(reviewId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Review", "id", reviewId.toString()));

        review.setDeletedAt(LocalDateTime.now());
        reviewRepository.save(review);
        log.info("Avis {} supprimé définitivement par un admin", reviewId);
    }
}
