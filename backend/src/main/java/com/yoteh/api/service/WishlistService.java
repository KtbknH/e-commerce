package com.yoteh.api.service;

import com.yoteh.api.dto.response.WishlistItemResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import java.util.UUID;

public interface WishlistService {

    PagedResponse<WishlistItemResponse> getWishlist(UUID userId, int page, int size);

    WishlistItemResponse addToWishlist(UUID userId, UUID productId);

    void removeFromWishlist(UUID userId, UUID productId);

    boolean isInWishlist(UUID userId, UUID productId);

    long getWishlistCount(UUID userId);
}
