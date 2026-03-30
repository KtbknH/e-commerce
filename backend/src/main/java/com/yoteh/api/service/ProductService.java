package com.yoteh.api.service;

import com.yoteh.api.dto.request.ProductRequest;
import com.yoteh.api.dto.response.ProductListResponse;
import com.yoteh.api.dto.response.ProductResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ProductService {

    // ═══════════════════════════════════════════════════════════
    //  ENDPOINTS PUBLICS
    // ═══════════════════════════════════════════════════════════

    /** Recherche multi-critères avec pagination et tri */
    PagedResponse<ProductListResponse> searchProducts(
            String search,
            UUID categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String size,
            String color,
            Boolean inStockOnly,
            Boolean isFeatured,
            Boolean onSale,
            int page,
            int size_,
            String sortBy,
            String sortDir);

    /** Détail produit par slug */
    ProductResponse getProductBySlug(String slug);

    /** Détail produit par ID */
    ProductResponse getProductById(UUID id);

    /** Produits d'une catégorie */
    PagedResponse<ProductListResponse> getProductsByCategory(
            UUID categoryId, int page, int size, String sortBy, String sortDir);

    /** Produits mis en avant (homepage) */
    List<ProductListResponse> getFeaturedProducts(int limit);

    /** Nouveautés */
    List<ProductListResponse> getNewArrivals(int limit);

    /** Produits en promotion */
    List<ProductListResponse> getOnSaleProducts(int limit);

    /** Filtres disponibles pour une catégorie */
    Map<String, Object> getAvailableFilters(UUID categoryId);

    // ═══════════════════════════════════════════════════════════
    //  ENDPOINTS ADMIN
    // ═══════════════════════════════════════════════════════════

    /** Liste admin avec filtres */
    PagedResponse<ProductListResponse> getAllProducts(
            String search, UUID categoryId, Boolean isActive, int page, int size);

    /** Créer un produit avec variantes et images */
    ProductResponse createProduct(ProductRequest request);

    /** Mettre à jour un produit */
    ProductResponse updateProduct(UUID id, ProductRequest request);

    /** Supprimer un produit */
    void deleteProduct(UUID id);

    /** Activer / désactiver un produit */
    ProductResponse toggleProductStatus(UUID id);

    /** Produits en rupture / stock bas (admin) */
    List<ProductListResponse> getLowStockProducts();
}
