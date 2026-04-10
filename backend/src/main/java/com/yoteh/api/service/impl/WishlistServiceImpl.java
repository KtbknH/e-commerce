package com.yoteh.api.service.impl;

import com.yoteh.api.dto.response.WishlistItemResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import com.yoteh.api.entity.Product;
import com.yoteh.api.entity.User;
import com.yoteh.api.entity.WishlistItem;
import com.yoteh.api.exception.DuplicateResourceException;
import com.yoteh.api.exception.ResourceNotFoundException;
import com.yoteh.api.mapper.WishlistItemMapper;
import com.yoteh.api.repository.ProductRepository;
import com.yoteh.api.repository.UserRepository;
import com.yoteh.api.repository.WishlistItemRepository;
import com.yoteh.api.service.WishlistService;
import com.yoteh.api.util.Constants;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final WishlistItemMapper wishlistItemMapper;

    // ─────────────────────────────────────────────────────────────
    //  GET WISHLIST
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<WishlistItemResponse> getWishlist(UUID userId, int page, int size) {
        int safeSize = Math.min(size, Constants.MAX_PAGE_SIZE);
        Page<WishlistItem> wishlistPage =
                wishlistItemRepository.findByUserIdOrderByCreatedAtDesc(
                        userId, PageRequest.of(page, safeSize));

        List<WishlistItemResponse> content =
                wishlistPage.getContent().stream()
                        .map(wishlistItemMapper::toResponse)
                        .collect(Collectors.toList());

        return PagedResponse.of(
                content,
                wishlistPage.getNumber(),
                wishlistPage.getSize(),
                wishlistPage.getTotalElements(),
                wishlistPage.getTotalPages(),
                wishlistPage.isLast());
    }

    // ─────────────────────────────────────────────────────────────
    //  ADD TO WISHLIST
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public WishlistItemResponse addToWishlist(UUID userId, UUID productId) {
        // Vérifier doublon
        if (wishlistItemRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new DuplicateResourceException("WishlistItem", "productId", productId.toString());
        }

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "User", "id", userId.toString()));

        Product product =
                productRepository
                        .findById(productId)
                        .filter(
                                p ->
                                        Boolean.TRUE.equals(p.getIsActive())
                                                && p.getDeletedAt() == null)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Product", "id", productId.toString()));

        WishlistItem item = new WishlistItem();
        item.setUser(user);
        item.setProduct(product);

        WishlistItem saved = wishlistItemRepository.save(item);
        log.debug("Produit {} ajouté à la wishlist de l'utilisateur {}", productId, userId);
        return wishlistItemMapper.toResponse(saved);
    }

    // ─────────────────────────────────────────────────────────────
    //  REMOVE FROM WISHLIST
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void removeFromWishlist(UUID userId, UUID productId) {
        WishlistItem item =
                wishlistItemRepository
                        .findByUserIdAndProductId(userId, productId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "WishlistItem", "productId", productId.toString()));

        wishlistItemRepository.delete(item);
        log.debug("Produit {} retiré de la wishlist de l'utilisateur {}", productId, userId);
    }

    // ─────────────────────────────────────────────────────────────
    //  IS IN WISHLIST
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public boolean isInWishlist(UUID userId, UUID productId) {
        return wishlistItemRepository.existsByUserIdAndProductId(userId, productId);
    }

    // ─────────────────────────────────────────────────────────────
    //  COUNT
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public long getWishlistCount(UUID userId) {
        return wishlistItemRepository.countByUserId(userId);
    }
}
