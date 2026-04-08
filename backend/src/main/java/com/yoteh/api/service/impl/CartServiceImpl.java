package com.yoteh.api.service.impl;

import com.yoteh.api.dto.request.CartItemRequest;
import com.yoteh.api.dto.response.CartResponse;
import com.yoteh.api.entity.Cart;
import com.yoteh.api.entity.CartItem;
import com.yoteh.api.entity.Product;
import com.yoteh.api.entity.ProductVariant;
import com.yoteh.api.entity.User;
import com.yoteh.api.exception.BadRequestException;
import com.yoteh.api.exception.ResourceNotFoundException;
import com.yoteh.api.mapper.CartMapper;
import com.yoteh.api.repository.CartItemRepository;
import com.yoteh.api.repository.CartRepository;
import com.yoteh.api.repository.ProductRepository;
import com.yoteh.api.repository.ProductVariantRepository;
import com.yoteh.api.repository.UserRepository;
import com.yoteh.api.service.CartService;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(UUID userId) {
        Cart cart =
                cartRepository
                        .findByUserIdWithItems(userId)
                        .orElseGet(() -> getOrCreateCart(userId));
        return cartMapper.toCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItem(UUID userId, CartItemRequest request) {
        Cart cart = getOrCreateCart(userId);

        Product product =
                productRepository
                        .findById(request.getProductId())
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Produit", "id", request.getProductId()));

        if (!product.getIsActive()) {
            throw new BadRequestException("Ce produit n'est plus disponible");
        }

        ProductVariant variant = null;
        BigDecimal unitPrice = product.getPrice();
        int availableStock = product.getStock();

        if (request.getVariantId() != null) {
            variant =
                    productVariantRepository
                            .findById(request.getVariantId())
                            .orElseThrow(
                                    () ->
                                            new ResourceNotFoundException(
                                                    "Variante", "id", request.getVariantId()));

            if (!variant.getIsActive()) {
                throw new BadRequestException("Cette variante n'est plus disponible");
            }
            if (!variant.getProduct().getId().equals(product.getId())) {
                throw new BadRequestException("La variante ne correspond pas au produit");
            }
            if (variant.getPrice() != null) {
                unitPrice = variant.getPrice();
            }
            availableStock = variant.getStock();
        } else if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            throw new BadRequestException("Ce produit nécessite de sélectionner une variante");
        }

        if (request.getQuantity() > availableStock) {
            throw new BadRequestException("Stock insuffisant. Disponible : " + availableStock);
        }

        // Vérifier si l'article existe déjà dans le panier
        var existingItem =
                cartItemRepository.findByCartIdAndProductIdAndVariantId(
                        cart.getId(), product.getId(), request.getVariantId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            if (newQuantity > availableStock) {
                throw new BadRequestException(
                        "Stock insuffisant. Disponible : "
                                + availableStock
                                + ", déjà dans le panier : "
                                + item.getQuantity());
            }
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setVariant(variant);
            newItem.setQuantity(request.getQuantity());
            newItem.setUnitPrice(unitPrice);
            cartItemRepository.save(newItem);
        }

        // Recharger le panier avec items
        Cart updatedCart = cartRepository.findByUserIdWithItems(userId).orElse(cart);
        return cartMapper.toCartResponse(updatedCart);
    }

    @Override
    @Transactional
    public CartResponse updateItemQuantity(UUID userId, UUID itemId, Integer quantity) {
        Cart cart = getOrCreateCart(userId);

        CartItem item =
                cartItemRepository
                        .findById(itemId)
                        .orElseThrow(() -> new ResourceNotFoundException("Article", "id", itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cet article n'appartient pas à votre panier");
        }

        int availableStock;
        if (item.getVariant() != null) {
            availableStock = item.getVariant().getStock();
        } else {
            availableStock = item.getProduct().getStock();
        }

        if (quantity > availableStock) {
            throw new BadRequestException("Stock insuffisant. Disponible : " + availableStock);
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        Cart updatedCart = cartRepository.findByUserIdWithItems(userId).orElse(cart);
        return cartMapper.toCartResponse(updatedCart);
    }

    @Override
    @Transactional
    public CartResponse removeItem(UUID userId, UUID itemId) {
        Cart cart = getOrCreateCart(userId);

        CartItem item =
                cartItemRepository
                        .findById(itemId)
                        .orElseThrow(() -> new ResourceNotFoundException("Article", "id", itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cet article n'appartient pas à votre panier");
        }

        cartItemRepository.delete(item);

        Cart updatedCart = cartRepository.findByUserIdWithItems(userId).orElse(cart);
        return cartMapper.toCartResponse(updatedCart);
    }

    @Override
    @Transactional
    public void clearCart(UUID userId) {
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.deleteAllByCartId(cart.getId());
        cart.setDiscountAmount(BigDecimal.ZERO);
        cart.setPromotionCode(null);
        cartRepository.save(cart);
    }

    // ─── Méthodes privées ───────────────────────────────────

    private Cart getOrCreateCart(UUID userId) {
        return cartRepository
                .findByUserId(userId)
                .orElseGet(
                        () -> {
                            User user =
                                    userRepository
                                            .findById(userId)
                                            .orElseThrow(
                                                    () ->
                                                            new ResourceNotFoundException(
                                                                    "Utilisateur", "id", userId));
                            Cart newCart = new Cart(user);
                            return cartRepository.save(newCart);
                        });
    }
}
