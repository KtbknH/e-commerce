package com.yoteh.api.service.impl;

import com.yoteh.api.dto.request.ProductRequest;
import com.yoteh.api.dto.request.ProductVariantRequest;
import com.yoteh.api.dto.response.ProductListResponse;
import com.yoteh.api.dto.response.ProductResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import com.yoteh.api.entity.Category;
import com.yoteh.api.entity.Product;
import com.yoteh.api.entity.ProductImage;
import com.yoteh.api.entity.ProductVariant;
import com.yoteh.api.exception.DuplicateResourceException;
import com.yoteh.api.exception.ResourceNotFoundException;
import com.yoteh.api.mapper.ProductMapper;
import com.yoteh.api.repository.CategoryRepository;
import com.yoteh.api.repository.ProductRepository;
import com.yoteh.api.service.ProductService;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    // ═══════════════════════════════════════════════════════════
    //  ENDPOINTS PUBLICS
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductListResponse> searchProducts(
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
            int pageSize,
            String sortBy,
            String sortDir) {

        Sort sort = buildSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        Page<Product> productPage =
                productRepository.searchProducts(
                        search,
                        categoryId,
                        minPrice,
                        maxPrice,
                        size,
                        color,
                        inStockOnly,
                        isFeatured,
                        onSale,
                        pageable);

        List<ProductListResponse> content =
                productPage.getContent().stream()
                        .map(productMapper::toListResponse)
                        .collect(Collectors.toList());

        return PagedResponse.<ProductListResponse>builder()
                .content(content)
                .page(productPage.getNumber())
                .size(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .last(productPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductBySlug(String slug) {
        Product product =
                productRepository
                        .findBySlug(slug)
                        .orElseThrow(() -> new ResourceNotFoundException("Product", "slug", slug));
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        Product product =
                productRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Product", "id", id.toString()));
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductListResponse> getProductsByCategory(
            UUID categoryId, int page, int size, String sortBy, String sortDir) {

        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category", "id", categoryId.toString());
        }

        Sort sort = buildSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage =
                productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable);

        List<ProductListResponse> content =
                productPage.getContent().stream()
                        .map(productMapper::toListResponse)
                        .collect(Collectors.toList());

        return PagedResponse.<ProductListResponse>builder()
                .content(content)
                .page(productPage.getNumber())
                .size(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .last(productPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductListResponse> getFeaturedProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findFeaturedProducts(pageable).stream()
                .map(productMapper::toListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductListResponse> getNewArrivals(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findNewArrivals(pageable).stream()
                .map(productMapper::toListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductListResponse> getOnSaleProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findOnSaleProducts(pageable).stream()
                .map(productMapper::toListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAvailableFilters(UUID categoryId) {
        Map<String, Object> filters = new HashMap<>();

        filters.put("sizes", productRepository.findAvailableSizes(categoryId));
        filters.put("colors", productRepository.findAvailableColors(categoryId));

        BigDecimal minPrice = productRepository.findMinPrice(categoryId);
        BigDecimal maxPrice = productRepository.findMaxPrice(categoryId);
        Map<String, BigDecimal> priceRange = new HashMap<>();
        priceRange.put("min", minPrice != null ? minPrice : BigDecimal.ZERO);
        priceRange.put("max", maxPrice != null ? maxPrice : BigDecimal.ZERO);
        filters.put("priceRange", priceRange);

        return filters;
    }

    // ═══════════════════════════════════════════════════════════
    //  ENDPOINTS ADMIN
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductListResponse> getAllProducts(
            String search, UUID categoryId, Boolean isActive, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> productPage =
                productRepository.findAllWithFilters(search, categoryId, isActive, pageable);

        List<ProductListResponse> content =
                productPage.getContent().stream()
                        .map(productMapper::toListResponse)
                        .collect(Collectors.toList());

        return PagedResponse.<ProductListResponse>builder()
                .content(content)
                .page(productPage.getNumber())
                .size(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .last(productPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Création du produit : {}", request.getName());

        // Vérifier la catégorie
        Category category =
                categoryRepository
                        .findById(request.getCategoryId())
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Category",
                                                "id",
                                                request.getCategoryId().toString()));

        // Vérifier l'unicité du SKU
        if (request.getSku() != null && productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Product", "sku", request.getSku());
        }

        // Créer le produit
        Product product = productMapper.toEntity(request);
        product.setCategory(category);
        product.setSlug(generateUniqueSlug(request.getName()));

        // Générer le SKU si non fourni
        if (product.getSku() == null || product.getSku().isEmpty()) {
            product.setSku(generateSku(category.getName(), request.getName()));
        }

        // Valeurs par défaut
        if (product.getCurrency() == null) product.setCurrency("XOF");
        if (product.getIsActive() == null) product.setIsActive(true);
        if (product.getIsFeatured() == null) product.setIsFeatured(false);
        if (product.getStock() == null) product.setStock(0);
        if (product.getLowStockThreshold() == null) product.setLowStockThreshold(5);

        // Ajouter les images
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            List<ProductImage> images = new ArrayList<>();
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                ProductImage image = new ProductImage();
                image.setProduct(product);
                image.setUrl(request.getImageUrls().get(i));
                image.setAltText(request.getName());
                image.setSortOrder(i);
                image.setIsPrimary(i == 0);
                images.add(image);
            }
            product.setImages(images);
        }

        // Ajouter les variantes
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            List<ProductVariant> variants = new ArrayList<>();
            for (ProductVariantRequest vr : request.getVariants()) {
                ProductVariant variant = new ProductVariant();
                variant.setProduct(product);
                variant.setSize(vr.getSize());
                variant.setColor(vr.getColor());
                variant.setSku(
                        vr.getSku() != null
                                ? vr.getSku()
                                : product.getSku() + "-" + (variants.size() + 1));
                variant.setPrice(vr.getPrice() != null ? vr.getPrice() : product.getPrice());
                variant.setCompareAtPrice(vr.getCompareAtPrice());
                variant.setStock(vr.getStockQuantity() != null ? vr.getStockQuantity() : 0);
                variant.setIsActive(vr.getIsActive() != null ? vr.getIsActive() : true);
                variants.add(variant);
            }
            product.setVariants(variants);
        }

        Product saved = productRepository.save(product);
        log.info("Produit créé : {} ({})", saved.getName(), saved.getId());
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(UUID id, ProductRequest request) {
        log.info("Mise à jour du produit : {}", id);

        Product product =
                productRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Product", "id", id.toString()));

        // Vérifier le SKU si modifié
        if (request.getSku() != null
                && !request.getSku().equals(product.getSku())
                && productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Product", "sku", request.getSku());
        }

        // Mettre à jour la catégorie si modifiée
        if (request.getCategoryId() != null
                && !request.getCategoryId().equals(product.getCategory().getId())) {
            Category category =
                    categoryRepository
                            .findById(request.getCategoryId())
                            .orElseThrow(
                                    () ->
                                            new ResourceNotFoundException(
                                                    "Category",
                                                    "id",
                                                    request.getCategoryId().toString()));
            product.setCategory(category);
        }

        // Mettre à jour les champs simples
        productMapper.updateEntityFromRequest(request, product);

        // Mettre à jour le slug si le nom change
        if (request.getName() != null && !request.getName().equals(product.getName())) {
            product.setSlug(generateUniqueSlug(request.getName()));
        }

        // Mettre à jour les images si fournies
        if (request.getImageUrls() != null) {
            if (product.getImages() != null) {
                product.getImages().clear();
            } else {
                product.setImages(new ArrayList<>());
            }
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                ProductImage image = new ProductImage();
                image.setProduct(product);
                image.setUrl(request.getImageUrls().get(i));
                image.setAltText(product.getName());
                image.setSortOrder(i);
                image.setIsPrimary(i == 0);
                product.getImages().add(image);
            }
        }

        // Mettre à jour les variantes si fournies
        if (request.getVariants() != null) {
            updateVariants(product, request.getVariants());
        }

        Product updated = productRepository.save(product);
        log.info("Produit mis à jour : {}", updated.getName());
        return productMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteProduct(UUID id) {
        Product product =
                productRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Product", "id", id.toString()));
        log.info("Suppression du produit : {} ({})", product.getName(), id);
        productRepository.delete(product);
    }

    @Override
    @Transactional
    public ProductResponse toggleProductStatus(UUID id) {
        Product product =
                productRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Product", "id", id.toString()));
        product.setIsActive(!product.getIsActive());
        Product updated = productRepository.save(product);
        log.info(
                "Produit {} {}", updated.getName(), updated.getIsActive() ? "activé" : "désactivé");
        return productMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductListResponse> getLowStockProducts() {
        return productRepository.findLowStockProducts().stream()
                .map(productMapper::toListResponse)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    //  MÉTHODES PRIVÉES
    // ═══════════════════════════════════════════════════════════

    private void updateVariants(Product product, List<ProductVariantRequest> variantRequests) {
        Map<UUID, ProductVariant> existingMap = new HashMap<>();
        if (product.getVariants() != null) {
            for (ProductVariant v : product.getVariants()) {
                existingMap.put(v.getId(), v);
            }
        }

        List<ProductVariant> updatedVariants = new ArrayList<>();

        for (ProductVariantRequest vr : variantRequests) {
            if (vr.getId() != null && existingMap.containsKey(vr.getId())) {
                // Mise à jour d'une variante existante
                ProductVariant existing = existingMap.get(vr.getId());
                if (vr.getSize() != null) existing.setSize(vr.getSize());
                if (vr.getColor() != null) existing.setColor(vr.getColor());
                if (vr.getSku() != null) existing.setSku(vr.getSku());
                if (vr.getPrice() != null) existing.setPrice(vr.getPrice());
                if (vr.getCompareAtPrice() != null)
                    existing.setCompareAtPrice(vr.getCompareAtPrice());
                if (vr.getStockQuantity() != null) existing.setStock(vr.getStockQuantity());
                if (vr.getIsActive() != null) existing.setIsActive(vr.getIsActive());
                updatedVariants.add(existing);
                existingMap.remove(vr.getId());
            } else {
                // Nouvelle variante
                ProductVariant newVariant = new ProductVariant();
                newVariant.setProduct(product);
                newVariant.setSize(vr.getSize());
                newVariant.setColor(vr.getColor());
                newVariant.setSku(
                        vr.getSku() != null
                                ? vr.getSku()
                                : product.getSku()
                                        + "-"
                                        + UUID.randomUUID().toString().substring(0, 6));
                newVariant.setPrice(vr.getPrice() != null ? vr.getPrice() : product.getPrice());
                newVariant.setCompareAtPrice(vr.getCompareAtPrice());
                newVariant.setStock(vr.getStockQuantity() != null ? vr.getStockQuantity() : 0);
                newVariant.setIsActive(vr.getIsActive() != null ? vr.getIsActive() : true);
                updatedVariants.add(newVariant);
            }
        }

        product.getVariants().clear();
        product.getVariants().addAll(updatedVariants);
    }

    private Sort buildSort(String sortBy, String sortDir) {
        String field =
                switch (sortBy != null ? sortBy : "createdAt") {
                    case "name" -> "name";
                    case "price" -> "price";
                    case "newest", "createdAt" -> "createdAt";
                    case "updated" -> "updatedAt";
                    default -> "createdAt";
                };

        return "asc".equalsIgnoreCase(sortDir)
                ? Sort.by(field).ascending()
                : Sort.by(field).descending();
    }

    private String generateUniqueSlug(String name) {
        String baseSlug = slugify(name);
        String slug = baseSlug;
        int counter = 1;
        while (productRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        return slug;
    }

    private String slugify(String input) {
        if (input == null) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    private String generateSku(String categoryName, String productName) {
        String catPrefix =
                categoryName != null && categoryName.length() >= 3
                        ? categoryName.substring(0, 3).toUpperCase()
                        : "GEN";
        String prodPrefix =
                productName != null && productName.length() >= 3
                        ? productName.substring(0, 3).toUpperCase()
                        : "PRD";
        String unique = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return catPrefix + "-" + prodPrefix + "-" + unique;
    }
}
