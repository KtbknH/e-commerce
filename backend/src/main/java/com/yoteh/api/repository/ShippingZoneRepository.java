package com.yoteh.api.repository;

import com.yoteh.api.entity.ShippingZone;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShippingZoneRepository extends JpaRepository<ShippingZone, UUID> {

    // ── Requêtes publiques (zones actives seulement) ──────────────────────────

    List<ShippingZone> findByIsActiveTrueAndDeletedAtIsNullOrderBySortOrderAsc();

    @Query(
            "SELECT sz FROM ShippingZone sz "
                    + "WHERE sz.isActive = true AND sz.deletedAt IS NULL "
                    + "AND LOWER(sz.cities) LIKE LOWER(CONCAT('%', :city, '%'))")
    List<ShippingZone> findActiveByCity(@Param("city") String city);

    List<ShippingZone> findByCountryAndIsActiveTrueAndDeletedAtIsNull(String country);

    Optional<ShippingZone> findByIdAndDeletedAtIsNull(UUID id);

    Optional<ShippingZone> findByIdAndIsActiveTrueAndDeletedAtIsNull(UUID id);

    boolean existsByNameAndDeletedAtIsNull(String name);

    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, UUID id);

    // ── Requêtes admin (toutes zones, paginées) ───────────────────────────────

    Page<ShippingZone> findByDeletedAtIsNullOrderBySortOrderAsc(Pageable pageable);

    @Query(
            "SELECT sz FROM ShippingZone sz "
                    + "WHERE sz.deletedAt IS NULL "
                    + "AND (:search IS NULL OR LOWER(sz.name) LIKE LOWER(CONCAT('%', :search, '%')) "
                    + "     OR LOWER(sz.country) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<ShippingZone> findAllWithSearch(@Param("search") String search, Pageable pageable);

    long countByIsActiveTrueAndDeletedAtIsNull();
}
