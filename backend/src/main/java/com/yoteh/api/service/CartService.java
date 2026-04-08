package com.yoteh.api.service;

import com.yoteh.api.dto.request.CartItemRequest;
import com.yoteh.api.dto.response.CartResponse;
import java.util.UUID;

public interface CartService {

    CartResponse getCart(UUID userId);

    CartResponse addItem(UUID userId, CartItemRequest request);

    CartResponse updateItemQuantity(UUID userId, UUID itemId, Integer quantity);

    CartResponse removeItem(UUID userId, UUID itemId);

    void clearCart(UUID userId);
}
