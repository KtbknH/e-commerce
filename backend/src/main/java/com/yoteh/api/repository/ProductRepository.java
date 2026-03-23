package com.yoteh.api.repository;

import com.yoteh.api.entity.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository
        extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    Optional<Product> findBySlug(String slug);

    Optional<Product> findBySku(String sku);

    boolean existsBySlug(String slug);

    boolean existsBySku(String sku);

    Page<Product> findByCategoryIdAndIsActiveTrueAndDeletedAtIsNull(
            UUID categoryId, Pageable pageable);

    Page<Product> findByIsActiveTrueAndDeletedAtIsNull(Pageable pageable);

    Page<Product> findByIsFeaturedTrueAndIsActiveTrueAndDeletedAtIsNull(Pageable pageable);

    Page<Product> findByIsNewTrueAndIsActiveTrueAndDeletedAtIsNull(Pageable pageable);

    @Query(
            "SELECT p FROM Product p WHERE p.isActive = true AND p.deletedAt IS NULL "
                    + "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) "
                    + "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) "
                    + "OR LOWER(p.tags) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Product> search(@Param("query") String query, Pageable pageable);

    @Query(
            "SELECT p FROM Product p WHERE p.isActive = true AND p.deletedAt IS NULL "
                    + "AND p.stock <= p.lowStockThreshold")
    List<Product> findLowStockProducts();

    @Query(
            "SELECT p FROM Product p WHERE p.isActive = true AND p.deletedAt IS NULL "
                    + "AND p.compareAtPrice IS NOT NULL AND p.compareAtPrice > p.price")
    Page<Product> findOnSaleProducts(Pageable pageable);

    @Query(
            "SELECT p FROM Product p WHERE p.isActive = true AND p.deletedAt IS NULL "
                    + "AND (:categoryId IS NULL OR p.category.id = :categoryId) "
                    + "AND (:minPrice IS NULL OR p.price >= :minPrice) "
                    + "AND (:maxPrice IS NULL OR p.price <= :maxPrice) "
                    + "AND (:brand IS NULL OR p.brand = :brand)")
    Page<Product> findFiltered(
            @Param("categoryId") UUID categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("brand") String brand,
            Pageable pageable);

    @Modifying
    @Query("UPDATE Product p SET p.viewsCount = p.viewsCount + 1 WHERE p.id = :id")
    void incrementViewsCount(@Param("id") UUID id);

    @Query(
            "SELECT DISTINCT p.brand FROM Product p WHERE p.brand IS NOT NULL AND p.isActive = true AND p.deletedAt IS NULL ORDER BY p.brand")
    List<String> findAllBrands();
}
