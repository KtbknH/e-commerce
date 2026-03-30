package com.yoteh.api.repository;

import com.yoteh.api.entity.Category;
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
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    // ─── Recherche par slug ─────────────────────────────────
    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsByName(String name);

    // ─── Catégories racines (sans parent) ───────────────────
    @Query(
            "SELECT c FROM Category c WHERE c.parent IS NULL AND c.isActive = true ORDER BY c.sortOrder ASC, c.name ASC")
    List<Category> findRootCategories();

    // ─── Catégories racines avec enfants (fetch join) ───────
    @Query(
            "SELECT DISTINCT c FROM Category c "
                    + "LEFT JOIN FETCH c.children ch "
                    + "WHERE c.parent IS NULL AND c.isActive = true "
                    + "ORDER BY c.sortOrder ASC, c.name ASC")
    List<Category> findRootCategoriesWithChildren();

    // ─── Enfants d'une catégorie ────────────────────────────
    @Query(
            "SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.isActive = true ORDER BY c.sortOrder ASC, c.name ASC")
    List<Category> findByParentId(@Param("parentId") UUID parentId);

    // ─── Par type ───────────────────────────────────────────
    List<Category> findByTypeAndIsActiveTrueOrderBySortOrderAsc(String type);

    // ─── Toutes les catégories actives ──────────────────────
    List<Category> findByIsActiveTrueOrderBySortOrderAscNameAsc();

    // ─── Admin : recherche avec filtres + pagination ────────
    @Query(
            "SELECT c FROM Category c WHERE "
                    + "(:search IS NULL OR :search = '' OR "
                    + " LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR "
                    + " LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%'))) "
                    + "AND (:type IS NULL OR :type = '' OR c.type = :type) "
                    + "AND (:isActive IS NULL OR c.isActive = :isActive) "
                    + "AND (:parentId IS NULL OR c.parent.id = :parentId)")
    Page<Category> findAllWithFilters(
            @Param("search") String search,
            @Param("type") String type,
            @Param("isActive") Boolean isActive,
            @Param("parentId") UUID parentId,
            Pageable pageable);

    // ─── Comptages ──────────────────────────────────────────
    long countByIsActiveTrue();

    long countByParentIsNull();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    long countProductsByCategoryId(@Param("categoryId") UUID categoryId);
}
