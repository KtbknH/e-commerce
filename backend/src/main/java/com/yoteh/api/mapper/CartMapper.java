package com.yoteh.api.mapper;

import com.yoteh.api.dto.response.CartItemResponse;
import com.yoteh.api.dto.response.CartResponse;
import com.yoteh.api.entity.Cart;
import com.yoteh.api.entity.CartItem;
import com.yoteh.api.entity.ProductImage;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "itemCount", expression = "java(cart.getItemCount())")
    @Mapping(target = "subtotal", expression = "java(cart.getSubtotal())")
    @Mapping(target = "total", expression = "java(cart.getTotal())")
    CartResponse toCartResponse(Cart cart);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productSlug", source = "product.slug")
    @Mapping(target = "productImage", source = "product.images", qualifiedByName = "primaryImage")
    @Mapping(target = "productSku", source = "product.sku")
    @Mapping(target = "variantId", source = "variant.id")
    @Mapping(target = "variantSku", source = "variant.sku")
    @Mapping(target = "size", source = "variant.size")
    @Mapping(target = "color", source = "variant.color")
    @Mapping(target = "lineTotal", expression = "java(cartItem.getLineTotal())")
    @Mapping(target = "availableStock", source = ".", qualifiedByName = "availableStock")
    @Mapping(target = "inStock", source = ".", qualifiedByName = "inStock")
    CartItemResponse toCartItemResponse(CartItem cartItem);

    List<CartItemResponse> toCartItemResponseList(List<CartItem> cartItems);

    @Named("primaryImage")
    default String primaryImage(List<ProductImage> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.stream()
                .filter(ProductImage::getIsPrimary)
                .findFirst()
                .map(ProductImage::getUrl)
                .orElse(images.get(0).getUrl());
    }

    @Named("availableStock")
    default Integer availableStock(CartItem cartItem) {
        if (cartItem.getVariant() != null) {
            return cartItem.getVariant().getStock();
        }
        return cartItem.getProduct() != null ? cartItem.getProduct().getStock() : 0;
    }

    @Named("inStock")
    default Boolean inStock(CartItem cartItem) {
        Integer stock = availableStock(cartItem);
        return stock != null && stock > 0;
    }
}
