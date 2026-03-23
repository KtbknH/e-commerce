package com.yoteh.api.repository;

import com.yoteh.api.entity.Review;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Page<Review> findByProductIdAndIsApprovedTrueAndDeletedAtIsNullOrderByCreatedAtDesc(
            UUID productId, Pageable pageable);

    Page<Review> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<Review> findByIsApprovedFalseAndDeletedAtIsNullOrderByCreatedAtAsc(Pageable pageable);

    Optional<Review> findByUserIdAndProductId(UUID userId, UUID productId);

    boolean existsByUserIdAndProductId(UUID userId, UUID productId);

    @Query(
            "SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.isApproved = true AND r.deletedAt IS NULL")
    Double getAverageRatingByProductId(@Param("productId") UUID productId);

    @Query(
            "SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.isApproved = true AND r.deletedAt IS NULL")
    long countApprovedByProductId(@Param("productId") UUID productId);
}
