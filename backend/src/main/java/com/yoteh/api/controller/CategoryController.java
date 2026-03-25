package com.yoteh.api.controller;

import com.yoteh.api.dto.request.CategoryRequest;
import com.yoteh.api.dto.response.CategoryResponse;
import com.yoteh.api.dto.response.common.ApiResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import com.yoteh.api.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Catégories", description = "Gestion du catalogue de catégories")
public class CategoryController {

  private final CategoryService categoryService;

  // ═══════════════════════════════════════════════════════════
  //  ENDPOINTS PUBLICS — /api/v1/categories
  // ═══════════════════════════════════════════════════════════

  @GetMapping("/categories/tree")
  @Operation(summary = "Arbre hiérarchique des catégories")
  public ResponseEntity<ApiResponse> getCategoryTree() {
    List<CategoryResponse> tree = categoryService.getCategoryTree();
    return ResponseEntity.ok(ApiResponse.success(tree, "Arbre de catégories récupéré"));
  }

  @GetMapping("/categories")
  @Operation(summary = "Toutes les catégories actives (liste plate)")
  public ResponseEntity<ApiResponse> getAllActiveCategories() {
    List<CategoryResponse> categories = categoryService.getAllActiveCategories();
    return ResponseEntity.ok(
        ApiResponse.success(categories, "Catégories récupérées"));
  }

  @GetMapping("/categories/roots")
  @Operation(summary = "Catégories racines uniquement")
  public ResponseEntity<ApiResponse> getRootCategories() {
    List<CategoryResponse> roots = categoryService.getRootCategories();
    return ResponseEntity.ok(ApiResponse.success(roots, "Catégories racines récupérées"));
  }

  @GetMapping("/categories/{id}/children")
  @Operation(summary = "Sous-catégories d'une catégorie parent")
  public ResponseEntity<ApiResponse> getSubCategories(@PathVariable UUID id) {
    List<CategoryResponse> children = categoryService.getSubCategories(id);
    return ResponseEntity.ok(
        ApiResponse.success(children, "Sous-catégories récupérées"));
  }

  @GetMapping("/categories/type/{type}")
  @Operation(summary = "Catégories par type (FEMME, HOMME, TECH...)")
  public ResponseEntity<ApiResponse> getCategoriesByType(@PathVariable String type) {
    List<CategoryResponse> categories = categoryService.getCategoriesByType(type);
    return ResponseEntity.ok(
        ApiResponse.success(categories, "Catégories par type récupérées"));
  }

  @GetMapping("/categories/slug/{slug}")
  @Operation(summary = "Détail d'une catégorie par slug")
  public ResponseEntity<ApiResponse> getCategoryBySlug(@PathVariable String slug) {
    CategoryResponse category = categoryService.getCategoryBySlug(slug);
    return ResponseEntity.ok(ApiResponse.success(category, "Catégorie récupérée"));
  }

  @GetMapping("/categories/{id}")
  @Operation(summary = "Détail d'une catégorie par ID")
  public ResponseEntity<ApiResponse> getCategoryById(@PathVariable UUID id) {
    CategoryResponse category = categoryService.getCategoryById(id);
    return ResponseEntity.ok(ApiResponse.success(category, "Catégorie récupérée"));
  }

  // ═══════════════════════════════════════════════════════════
  //  ENDPOINTS ADMIN — /api/v1/admin/categories
  // ═══════════════════════════════════════════════════════════

  @GetMapping("/admin/categories")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "[Admin] Liste paginée des catégories avec filtres")
  public ResponseEntity<ApiResponse> adminGetAllCategories(
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) Boolean isActive,
      @RequestParam(required = false) UUID parentId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    PagedResponse<CategoryResponse> result =
        categoryService.getAllCategories(search, type, isActive, parentId, page, size);
    return ResponseEntity.ok(ApiResponse.success(result, "Catégories récupérées"));
  }

  @PostMapping("/admin/categories")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "[Admin] Créer une catégorie")
  public ResponseEntity<ApiResponse> createCategory(
      @Valid @RequestBody CategoryRequest request) {
    CategoryResponse created = categoryService.createCategory(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(created, "Catégorie créée avec succès"));
  }

  @PutMapping("/admin/categories/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "[Admin] Mettre à jour une catégorie")
  public ResponseEntity<ApiResponse> updateCategory(
      @PathVariable UUID id, @Valid @RequestBody CategoryRequest request) {
    CategoryResponse updated = categoryService.updateCategory(id, request);
    return ResponseEntity.ok(ApiResponse.success(updated, "Catégorie mise à jour"));
  }

  @DeleteMapping("/admin/categories/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "[Admin] Supprimer une catégorie")
  public ResponseEntity<ApiResponse> deleteCategory(@PathVariable UUID id) {
    categoryService.deleteCategory(id);
    return ResponseEntity.ok(ApiResponse.success(null, "Catégorie supprimée"));
  }

  @PatchMapping("/admin/categories/{id}/toggle")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "[Admin] Activer/désactiver une catégorie")
  public ResponseEntity<ApiResponse> toggleCategoryStatus(@PathVariable UUID id) {
    CategoryResponse toggled = categoryService.toggleCategoryStatus(id);
    return ResponseEntity.ok(ApiResponse.success(toggled, "Statut de la catégorie modifié"));
  }
}