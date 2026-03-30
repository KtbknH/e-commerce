package com.yoteh.api.repository;

import com.yoteh.api.entity.Product;
import java.math.BigDecimal;
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
public interface ProductRepository extends JpaRepository<Product, UUID> {

    // ─── Recherche par slug / SKU ───────────────────────────
    Optional<Product> findBySlug(String slug);

    Optional<Product> findBySku(String sku);

    boolean existsBySlug(String slug);

    boolean existsBySku(String sku);

    // ─── Par catégorie ──────────────────────────────────────
    Page<Product> findByCategoryIdAndIsActiveTrue(UUID categoryId, Pageable pageable);

    List<Product> findByCategoryIdAndIsActiveTrue(UUID categoryId);

    // ─── Produits mis en avant ──────────────────────────────
    @Query(
            "SELECT p FROM Product p WHERE p.isFeatured = true AND p.isActive = true"
                    + " ORDER BY p.updatedAt DESC")
    List<Product> findFeaturedProducts(Pageable pageable);

    // ─── Nouveaux produits ──────────────────────────────────
    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.createdAt DESC")
    List<Product> findNewArrivals(Pageable pageable);

    // ─── Produits en promotion ──────────────────────────────
    @Query(
            "SELECT p FROM Product p WHERE p.isActive = true"
                    + " AND p.compareAtPrice IS NOT NULL"
                    + " AND p.compareAtPrice > p.price"
                    + " ORDER BY p.updatedAt DESC")
    List<Product> findOnSaleProducts(Pageable pageable);

    // ─── Alerte stock bas ───────────────────────────────────
    @Query(
            "SELECT p FROM Product p WHERE p.isActive = true"
                    + " AND p.stock <= p.lowStockThreshold"
                    + " ORDER BY p.stock ASC")
    List<Product> findLowStockProducts();

    // ═══════════════════════════════════════════════════════════
    //  RECHERCHE FULL-TEXT + FILTRES MULTI-CRITÈRES
    // ═══════════════════════════════════════════════════════════

    @Query(
            "SELECT DISTINCT p FROM Product p"
                    + " LEFT JOIN p.variants v"
                    + " WHERE p.isActive = true"
                    + " AND (:search IS NULL OR :search = '' OR"
                    + "   LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR"
                    + "   LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) OR"
                    + "   LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')))"
                    + " AND (:categoryId IS NULL OR p.category.id = :categoryId)"
                    + " AND (:minPrice IS NULL OR p.price >= :minPrice)"
                    + " AND (:maxPrice IS NULL OR p.price <= :maxPrice)"
                    + " AND (:size IS NULL OR :size = '' OR v.size = :size)"
                    + " AND (:color IS NULL OR :color = '' OR v.color = :color)"
                    + " AND (:inStockOnly IS NULL OR :inStockOnly = false OR p.stock > 0)"
                    + " AND (:isFeatured IS NULL OR p.isFeatured = :isFeatured)"
                    + " AND (:onSale IS NULL OR :onSale = false OR"
                    + "   (p.compareAtPrice IS NOT NULL AND p.compareAtPrice > p.price))")
    Page<Product> searchProducts(
            @Param("search") String search,
            @Param("categoryId") UUID categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("size") String size,
            @Param("color") String color,
            @Param("inStockOnly") Boolean inStockOnly,
            @Param("isFeatured") Boolean isFeatured,
            @Param("onSale") Boolean onSale,
            Pageable pageable);

    // ─── Admin : tous les produits avec filtres ─────────────
    @Query(
            "SELECT p FROM Product p WHERE"
                    + " (:search IS NULL OR :search = '' OR"
                    + "  LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR"
                    + "  LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')))"
                    + " AND (:categoryId IS NULL OR p.category.id = :categoryId)"
                    + " AND (:isActive IS NULL OR p.isActive = :isActive)")
    Page<Product> findAllWithFilters(
            @Param("search") String search,
            @Param("categoryId") UUID categoryId,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    // ─── Tailles et couleurs disponibles (pour les filtres) ─
    @Query(
            "SELECT DISTINCT v.size FROM ProductVariant v"
                    + " WHERE v.product.isActive = true"
                    + " AND v.isActive = true"
                    + " AND v.size IS NOT NULL"
                    + " AND (:categoryId IS NULL OR v.product.category.id = :categoryId)"
                    + " ORDER BY v.size")
    List<String> findAvailableSizes(@Param("categoryId") UUID categoryId);

    @Query(
            "SELECT DISTINCT v.color FROM ProductVariant v"
                    + " WHERE v.product.isActive = true"
                    + " AND v.isActive = true"
                    + " AND v.color IS NOT NULL"
                    + " AND (:categoryId IS NULL OR v.product.category.id = :categoryId)"
                    + " ORDER BY v.color")
    List<String> findAvailableColors(@Param("categoryId") UUID categoryId);

    // ─── Plage de prix (pour les filtres) ───────────────────
    @Query(
            "SELECT MIN(p.price) FROM Product p"
                    + " WHERE p.isActive = true"
                    + " AND (:categoryId IS NULL OR p.category.id = :categoryId)")
    BigDecimal findMinPrice(@Param("categoryId") UUID categoryId);

    @Query(
            "SELECT MAX(p.price) FROM Product p"
                    + " WHERE p.isActive = true"
                    + " AND (:categoryId IS NULL OR p.category.id = :categoryId)")
    BigDecimal findMaxPrice(@Param("categoryId") UUID categoryId);

    // ─── Comptages ──────────────────────────────────────────
    long countByIsActiveTrue();

    long countByIsFeaturedTrue();

    @Query(
            "SELECT COUNT(p) FROM Product p WHERE p.isActive = true"
                    + " AND p.stock <= p.lowStockThreshold")
    long countLowStockProducts();
}
