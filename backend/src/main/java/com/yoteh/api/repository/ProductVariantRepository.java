package com.yoteh.api.repository;

import com.yoteh.api.entity.ProductVariant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    List<ProductVariant> findByProductIdAndIsActiveTrueAndDeletedAtIsNullOrderBySortOrderAsc(
            UUID productId);

    Optional<ProductVariant> findBySku(String sku);

    boolean existsBySku(String sku);

    Optional<ProductVariant> findByProductIdAndSizeAndColor(
            UUID productId, String size, String color);
}
