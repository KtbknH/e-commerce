package com.yoteh.api.repository;

import com.yoteh.api.entity.Category;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Category> findByParentIsNullAndIsActiveTrueAndDeletedAtIsNullOrderBySortOrderAsc();

    List<Category> findByParentIdAndIsActiveTrueAndDeletedAtIsNullOrderBySortOrderAsc(
            UUID parentId);

    List<Category> findByIsFeaturedTrueAndIsActiveTrueAndDeletedAtIsNullOrderBySortOrderAsc();

    @Query(
            "SELECT c FROM Category c WHERE c.isActive = true AND c.deletedAt IS NULL ORDER BY c.sortOrder ASC")
    List<Category> findAllActive();

    @Query(
            "SELECT c FROM Category c LEFT JOIN FETCH c.children WHERE c.parent IS NULL AND c.isActive = true AND c.deletedAt IS NULL ORDER BY c.sortOrder ASC")
    List<Category> findAllRootsWithChildren();
}
