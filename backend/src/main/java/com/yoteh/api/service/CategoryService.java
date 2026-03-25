package com.yoteh.api.service;

import com.yoteh.api.dto.request.CategoryRequest;
import com.yoteh.api.dto.response.CategoryResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import java.util.List;
import java.util.UUID;

public interface CategoryService {

  // ═══════════════════════════════════════════════════════════
  //  ENDPOINTS PUBLICS
  // ═══════════════════════════════════════════════════════════

  /** Arbre hiérarchique complet (racines + enfants récursifs) */
  List<CategoryResponse> getCategoryTree();

  /** Toutes les catégories actives (liste plate) */
  List<CategoryResponse> getAllActiveCategories();

  /** Catégories racines uniquement */
  List<CategoryResponse> getRootCategories();

  /** Sous-catégories d'un parent */
  List<CategoryResponse> getSubCategories(UUID parentId);

  /** Catégories par type (FEMME, HOMME, TECH...) */
  List<CategoryResponse> getCategoriesByType(String type);

  /** Détail d'une catégorie par slug */
  CategoryResponse getCategoryBySlug(String slug);

  /** Détail d'une catégorie par ID */
  CategoryResponse getCategoryById(UUID id);

  // ═══════════════════════════════════════════════════════════
  //  ENDPOINTS ADMIN
  // ═══════════════════════════════════════════════════════════

  /** Liste paginée avec filtres (admin) */
  PagedResponse<CategoryResponse> getAllCategories(
      String search, String type, Boolean isActive, UUID parentId, int page, int size);

  /** Créer une catégorie */
  CategoryResponse createCategory(CategoryRequest request);

  /** Mettre à jour une catégorie */
  CategoryResponse updateCategory(UUID id, CategoryRequest request);

  /** Supprimer une catégorie (soft ou hard) */
  void deleteCategory(UUID id);

  /** Activer / désactiver une catégorie */
  CategoryResponse toggleCategoryStatus(UUID id);
}