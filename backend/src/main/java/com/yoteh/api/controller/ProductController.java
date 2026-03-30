package com.yoteh.api.controller;

import com.yoteh.api.dto.request.ProductRequest;
import com.yoteh.api.dto.response.ProductListResponse;
import com.yoteh.api.dto.response.ProductResponse;
import com.yoteh.api.dto.response.common.ApiResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import com.yoteh.api.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
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
@Tag(name = "Produits", description = "Catalogue de produits")
public class ProductController {

    private final ProductService productService;

    // ═══════════════════════════════════════════════════════════
    //  ENDPOINTS PUBLICS — /api/v1/products
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/products")
    @Operation(summary = "Recherche et liste des produits avec filtres")
    public ResponseEntity<ApiResponse> searchProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) Boolean inStockOnly,
            @RequestParam(required = false) Boolean isFeatured,
            @RequestParam(required = false) Boolean onSale,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        PagedResponse<ProductListResponse> result =
                productService.searchProducts(
                        search,
                        categoryId,
                        minPrice,
                        maxPrice,
                        size,
                        color,
                        inStockOnly,
                        isFeatured,
                        onSale,
                        page,
                        pageSize,
                        sortBy,
                        sortDir);
        return ResponseEntity.ok(ApiResponse.success(result, "Produits récupérés"));
    }

    @GetMapping("/products/slug/{slug}")
    @Operation(summary = "Détail d'un produit par slug")
    public ResponseEntity<ApiResponse> getProductBySlug(@PathVariable String slug) {
        ProductResponse product = productService.getProductBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(product, "Produit récupéré"));
    }

    @GetMapping("/products/{id}")
    @Operation(summary = "Détail d'un produit par ID")
    public ResponseEntity<ApiResponse> getProductById(@PathVariable UUID id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product, "Produit récupéré"));
    }

    @GetMapping("/products/category/{categoryId}")
    @Operation(summary = "Produits d'une catégorie")
    public ResponseEntity<ApiResponse> getProductsByCategory(
            @PathVariable UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        PagedResponse<ProductListResponse> result =
                productService.getProductsByCategory(categoryId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(result, "Produits par catégorie"));
    }

    @GetMapping("/products/featured")
    @Operation(summary = "Produits mis en avant (homepage)")
    public ResponseEntity<ApiResponse> getFeaturedProducts(
            @RequestParam(defaultValue = "10") int limit) {
        List<ProductListResponse> products = productService.getFeaturedProducts(limit);
        return ResponseEntity.ok(ApiResponse.success(products, "Produits mis en avant"));
    }

    @GetMapping("/products/new-arrivals")
    @Operation(summary = "Nouveaux produits")
    public ResponseEntity<ApiResponse> getNewArrivals(
            @RequestParam(defaultValue = "10") int limit) {
        List<ProductListResponse> products = productService.getNewArrivals(limit);
        return ResponseEntity.ok(ApiResponse.success(products, "Nouveaux produits"));
    }

    @GetMapping("/products/on-sale")
    @Operation(summary = "Produits en promotion")
    public ResponseEntity<ApiResponse> getOnSaleProducts(
            @RequestParam(defaultValue = "10") int limit) {
        List<ProductListResponse> products = productService.getOnSaleProducts(limit);
        return ResponseEntity.ok(ApiResponse.success(products, "Produits en promotion"));
    }

    @GetMapping("/products/filters")
    @Operation(summary = "Filtres disponibles (tailles, couleurs, plage de prix)")
    public ResponseEntity<ApiResponse> getAvailableFilters(
            @RequestParam(required = false) UUID categoryId) {
        Map<String, Object> filters = productService.getAvailableFilters(categoryId);
        return ResponseEntity.ok(ApiResponse.success(filters, "Filtres disponibles"));
    }

    // ═══════════════════════════════════════════════════════════
    //  ENDPOINTS ADMIN — /api/v1/admin/products
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/admin/products")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Liste paginée des produits avec filtres")
    public ResponseEntity<ApiResponse> adminGetAllProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PagedResponse<ProductListResponse> result =
                productService.getAllProducts(search, categoryId, isActive, page, size);
        return ResponseEntity.ok(ApiResponse.success(result, "Produits récupérés (admin)"));
    }

    @PostMapping("/admin/products")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Créer un produit")
    public ResponseEntity<ApiResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse created = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Produit créé avec succès"));
    }

    @PutMapping("/admin/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Mettre à jour un produit")
    public ResponseEntity<ApiResponse> updateProduct(
            @PathVariable UUID id, @Valid @RequestBody ProductRequest request) {
        ProductResponse updated = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Produit mis à jour"));
    }

    @DeleteMapping("/admin/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Supprimer un produit")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Produit supprimé"));
    }

    @PatchMapping("/admin/products/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Activer/désactiver un produit")
    public ResponseEntity<ApiResponse> toggleProductStatus(@PathVariable UUID id) {
        ProductResponse toggled = productService.toggleProductStatus(id);
        return ResponseEntity.ok(ApiResponse.success(toggled, "Statut du produit modifié"));
    }

    @GetMapping("/admin/products/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Produits en stock bas")
    public ResponseEntity<ApiResponse> getLowStockProducts() {
        List<ProductListResponse> products = productService.getLowStockProducts();
        return ResponseEntity.ok(ApiResponse.success(products, "Produits en stock bas"));
    }
}
