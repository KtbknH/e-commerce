package com.yoteh.api.repository;

import com.yoteh.api.entity.Promotion;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, UUID> {

    Optional<Promotion> findByCode(String code);

    boolean existsByCode(String code);

    @Query(
            "SELECT p FROM Promotion p WHERE p.isActive = true AND p.deletedAt IS NULL "
                    + "AND p.startsAt <= :now AND p.endsAt >= :now "
                    + "AND (p.maxUses IS NULL OR p.usedCount < p.maxUses)")
    List<Promotion> findActivePromotions(LocalDateTime now);

    @Query(
            "SELECT p FROM Promotion p WHERE p.isActive = true AND p.deletedAt IS NULL "
                    + "AND p.isFlashSale = true AND p.startsAt <= :now AND p.endsAt >= :now")
    List<Promotion> findActiveFlashSales(LocalDateTime now);

    Page<Promotion> findByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);
}
