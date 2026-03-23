package com.yoteh.api.repository;

import com.yoteh.api.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    List<ProductVariant> findByProductIdAndIsActiveTrueAndDeletedAtIsNullOrderBySortOrderAsc(UUID productId);

    Optional<ProductVariant> findBySku(String sku);

    boolean existsBySku(String sku);

    Optional<ProductVariant> findByProductIdAndSizeAndColor(UUID productId, String size, String color);
}
