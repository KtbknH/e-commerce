package com.yoteh.api.repository;

import com.yoteh.api.entity.Promotion;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, UUID> {

    // ── Lookup par code ──

    Optional<Promotion> findByCodeAndDeletedAtIsNull(String code);

    boolean existsByCodeAndDeletedAtIsNull(String code);

    /** Utilisé en mise à jour pour exclure la promotion courante du check d'unicité. */
    boolean existsByCodeAndIdNotAndDeletedAtIsNull(String code, UUID id);

    // ── Promotions actives ──

    /**
     * Promotions valides à un instant donné : actives, dans leur plage de dates et n'ayant pas
     * atteint leur limite d'utilisation.
     */
    @Query(
            "SELECT p FROM Promotion p"
                    + " WHERE p.isActive = true AND p.deletedAt IS NULL"
                    + " AND p.startsAt <= :now AND p.endsAt >= :now"
                    + " AND (p.maxUses IS NULL OR p.usedCount < p.maxUses)")
    List<Promotion> findActivePromotions(@Param("now") LocalDateTime now);

    /** Ventes flash actives uniquement. */
    @Query(
            "SELECT p FROM Promotion p"
                    + " WHERE p.isActive = true AND p.deletedAt IS NULL"
                    + " AND p.isFlashSale = true"
                    + " AND p.startsAt <= :now AND p.endsAt >= :now")
    List<Promotion> findActiveFlashSales(@Param("now") LocalDateTime now);

    // ── Liste admin avec filtres ──

    @Query(
            "SELECT p FROM Promotion p"
                    + " WHERE p.deletedAt IS NULL"
                    + " AND (:isActive IS NULL OR p.isActive = :isActive)"
                    + " AND (:isFlashSale IS NULL OR p.isFlashSale = :isFlashSale)"
                    + " AND (:search IS NULL"
                    + "      OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))"
                    + "      OR LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Promotion> findAllWithFilters(
            @Param("isActive") Boolean isActive,
            @Param("isFlashSale") Boolean isFlashSale,
            @Param("search") String search,
            Pageable pageable);

    // ── Mutation atomique ──

    /** Incrémente usedCount de façon atomique — appelé lors de l'application d'un code promo. */
    @Modifying
    @Query("UPDATE Promotion p SET p.usedCount = p.usedCount + 1 WHERE p.id = :id")
    void incrementUsedCount(@Param("id") UUID id);
}
