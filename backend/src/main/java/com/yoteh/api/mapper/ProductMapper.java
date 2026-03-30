package com.yoteh.api.mapper;

import com.yoteh.api.dto.request.ProductRequest;
import com.yoteh.api.dto.response.ProductImageResponse;
import com.yoteh.api.dto.response.ProductListResponse;
import com.yoteh.api.dto.response.ProductResponse;
import com.yoteh.api.dto.response.ProductVariantResponse;
import com.yoteh.api.entity.Product;
import com.yoteh.api.entity.ProductImage;
import com.yoteh.api.entity.ProductVariant;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {

    // ═══════════════════════════════════════════════════════════
    //  PRODUCT → PRODUCT RESPONSE (détail complet)
    // ═══════════════════════════════════════════════════════════

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "categorySlug", source = "category.slug")
    @Mapping(target = "primaryImageUrl", source = ".", qualifiedByName = "extractPrimaryImage")
    @Mapping(target = "images", source = "images", qualifiedByName = "mapImages")
    @Mapping(target = "variants", source = "variants", qualifiedByName = "mapVariants")
    @Mapping(target = "variantCount", source = ".", qualifiedByName = "countVariants")
    @Mapping(target = "inStock", source = ".", qualifiedByName = "checkInStock")
    @Mapping(target = "discountPercent", source = ".", qualifiedByName = "calcDiscount")
    @Mapping(target = "stockQuantity", source = "stock")
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    ProductResponse toResponse(Product product);

    // ═══════════════════════════════════════════════════════════
    //  PRODUCT → PRODUCT LIST RESPONSE (liste légère)
    // ═══════════════════════════════════════════════════════════

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "primaryImageUrl", source = ".", qualifiedByName = "extractPrimaryImage")
    @Mapping(target = "variantCount", source = ".", qualifiedByName = "countVariants")
    @Mapping(target = "inStock", source = ".", qualifiedByName = "checkInStock")
    @Mapping(target = "discountPercent", source = ".", qualifiedByName = "calcDiscount")
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    ProductListResponse toListResponse(Product product);

    // ═══════════════════════════════════════════════════════════
    //  REQUEST → ENTITY (création)
    // ═══════════════════════════════════════════════════════════

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "stock", source = "stockQuantity")
    Product toEntity(ProductRequest request);

    // ═══════════════════════════════════════════════════════════
    //  REQUEST → ENTITY (mise à jour)
    // ═══════════════════════════════════════════════════════════

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "stock", source = "stockQuantity")
    void updateEntityFromRequest(ProductRequest request, @MappingTarget Product product);

    // ═══════════════════════════════════════════════════════════
    //  SOUS-MAPPINGS : Image, Variant
    // ═══════════════════════════════════════════════════════════

    default ProductImageResponse toImageResponse(ProductImage image) {
        if (image == null) return null;
        return ProductImageResponse.builder()
                .id(image.getId())
                .imageUrl(image.getUrl())
                .altText(image.getAltText())
                .sortOrder(image.getSortOrder())
                .isPrimary(image.getIsPrimary())
                .build();
    }

    default ProductVariantResponse toVariantResponse(ProductVariant variant) {
        if (variant == null) return null;
        return ProductVariantResponse.builder()
                .id(variant.getId())
                .size(variant.getSize())
                .color(variant.getColor())
                .sku(variant.getSku())
                .price(variant.getPrice())
                .compareAtPrice(variant.getCompareAtPrice())
                .stockQuantity(variant.getStock())
                .isActive(variant.getIsActive())
                .inStock(variant.getStock() != null && variant.getStock() > 0)
                .build();
    }

    // ═══════════════════════════════════════════════════════════
    //  MÉTHODES UTILITAIRES (qualifiedByName)
    // ═══════════════════════════════════════════════════════════

    @Named("extractPrimaryImage")
    default String extractPrimaryImage(Product product) {
        if (product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }
        return product.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .findFirst()
                .map(ProductImage::getUrl)
                .orElse(product.getImages().get(0).getUrl());
    }

    @Named("mapImages")
    default List<ProductImageResponse> mapImages(List<ProductImage> images) {
        if (images == null) return Collections.emptyList();
        return images.stream()
                .sorted(
                        (a, b) -> {
                            int sa = a.getSortOrder() != null ? a.getSortOrder() : 0;
                            int sb = b.getSortOrder() != null ? b.getSortOrder() : 0;
                            return Integer.compare(sa, sb);
                        })
                .map(this::toImageResponse)
                .collect(Collectors.toList());
    }

    @Named("mapVariants")
    default List<ProductVariantResponse> mapVariants(List<ProductVariant> variants) {
        if (variants == null) return Collections.emptyList();
        return variants.stream()
                .filter(v -> Boolean.TRUE.equals(v.getIsActive()))
                .map(this::toVariantResponse)
                .collect(Collectors.toList());
    }

    @Named("countVariants")
    default int countVariants(Product product) {
        return product.getVariants() != null ? product.getVariants().size() : 0;
    }

    @Named("checkInStock")
    default Boolean checkInStock(Product product) {
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            return product.getVariants().stream()
                    .anyMatch(
                            v ->
                                    Boolean.TRUE.equals(v.getIsActive())
                                            && v.getStock() != null
                                            && v.getStock() > 0);
        }
        return product.getStock() != null && product.getStock() > 0;
    }

    @Named("calcDiscount")
    default Integer calcDiscount(Product product) {
        if (product.getCompareAtPrice() == null
                || product.getPrice() == null
                || product.getCompareAtPrice().compareTo(product.getPrice()) <= 0) {
            return null;
        }
        BigDecimal diff = product.getCompareAtPrice().subtract(product.getPrice());
        BigDecimal percent =
                diff.multiply(BigDecimal.valueOf(100))
                        .divide(product.getCompareAtPrice(), 0, RoundingMode.HALF_UP);
        return percent.intValue();
    }

    default List<ProductListResponse> toListResponses(List<Product> products) {
        if (products == null) return Collections.emptyList();
        return products.stream().map(this::toListResponse).collect(Collectors.toList());
    }
}
