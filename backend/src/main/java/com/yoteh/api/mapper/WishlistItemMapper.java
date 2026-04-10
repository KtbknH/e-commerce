package com.yoteh.api.mapper;

import com.yoteh.api.dto.response.WishlistItemResponse;
import com.yoteh.api.entity.ProductImage;
import com.yoteh.api.entity.WishlistItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface WishlistItemMapper {

    default WishlistItemResponse toResponse(WishlistItem item) {
        if (item == null) return null;

        WishlistItemResponse response = new WishlistItemResponse();
        response.setId(item.getId());
        response.setAddedAt(item.getCreatedAt());

        if (item.getProduct() != null) {
            var product = item.getProduct();

            response.setProductId(product.getId());
            response.setProductName(product.getName());
            response.setProductSlug(product.getSlug());
            response.setProductPrice(product.getPrice());
            response.setProductCompareAtPrice(product.getCompareAtPrice());
            response.setProductCurrency(product.getCurrency());
            response.setProductIsFeatured(product.getIsFeatured());
            response.setProductInStock(product.getStock() != null && product.getStock() > 0);

            // Image principale (url — champ exact de ProductImage)
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                String imgUrl =
                        product.getImages().stream()
                                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                                .findFirst()
                                .map(ProductImage::getUrl)
                                .orElse(product.getImages().get(0).getUrl());
                response.setProductPrimaryImageUrl(imgUrl);
            }

            // Pourcentage de réduction
            BigDecimal price = product.getPrice();
            BigDecimal compareAt = product.getCompareAtPrice();
            if (price != null && compareAt != null && compareAt.compareTo(price) > 0) {
                int discount =
                        compareAt
                                .subtract(price)
                                .multiply(BigDecimal.valueOf(100))
                                .divide(compareAt, 0, RoundingMode.HALF_UP)
                                .intValue();
                response.setProductDiscountPercent(discount);
            }

            // Catégorie
            if (product.getCategory() != null) {
                response.setCategoryId(product.getCategory().getId());
                response.setCategoryName(product.getCategory().getName());
            }
        }

        return response;
    }
}
