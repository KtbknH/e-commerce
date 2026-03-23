package com.yoteh.api.repository;

import com.yoteh.api.entity.ShippingZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShippingZoneRepository extends JpaRepository<ShippingZone, UUID> {

    List<ShippingZone> findByIsActiveTrueAndDeletedAtIsNullOrderBySortOrderAsc();

    @Query("SELECT sz FROM ShippingZone sz WHERE sz.isActive = true AND sz.deletedAt IS NULL " +
            "AND LOWER(sz.cities) LIKE LOWER(CONCAT('%', :city, '%'))")
    List<ShippingZone> findByCity(@Param("city") String city);

    List<ShippingZone> findByCountryAndIsActiveTrueAndDeletedAtIsNull(String country);
}
