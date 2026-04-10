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

    // Avis approuvés d'un produit (public)
    @Query(
            "SELECT r FROM Review r WHERE r.product.id = :productId "
                    + "AND r.isApproved = true AND r.deletedAt IS NULL "
                    + "ORDER BY r.createdAt DESC")
    Page<Review> findApprovedByProductId(@Param("productId") UUID productId, Pageable pageable);

    // Avis d'un utilisateur
    Page<Review> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    // Avis en attente de modération (admin)
    Page<Review> findByIsApprovedFalseAndDeletedAtIsNullOrderByCreatedAtAsc(Pageable pageable);

    // Tous les avis avec filtre optionnel (admin)
    @Query(
            "SELECT r FROM Review r WHERE r.deletedAt IS NULL "
                    + "AND (:approved IS NULL OR r.isApproved = :approved) "
                    + "ORDER BY r.createdAt DESC")
    Page<Review> findAllFiltered(@Param("approved") Boolean approved, Pageable pageable);

    // Vérifier si l'utilisateur a déjà laissé un avis sur ce produit
    Optional<Review> findByUserIdAndProductIdAndDeletedAtIsNull(UUID userId, UUID productId);

    boolean existsByUserIdAndProductIdAndDeletedAtIsNull(UUID userId, UUID productId);

    // Statistiques
    @Query(
            "SELECT AVG(r.rating) FROM Review r "
                    + "WHERE r.product.id = :productId AND r.isApproved = true AND r.deletedAt IS NULL")
    Double getAverageRatingByProductId(@Param("productId") UUID productId);

    @Query(
            "SELECT COUNT(r) FROM Review r "
                    + "WHERE r.product.id = :productId AND r.isApproved = true AND r.deletedAt IS NULL")
    long countApprovedByProductId(@Param("productId") UUID productId);
}
