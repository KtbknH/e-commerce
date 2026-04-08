package com.yoteh.api.service;

import com.yoteh.api.dto.request.OrderRequest;
import com.yoteh.api.dto.response.OrderListResponse;
import com.yoteh.api.dto.response.OrderResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import com.yoteh.api.entity.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public interface OrderService {

    OrderResponse createOrder(UUID userId, OrderRequest request);

    OrderResponse getOrderById(UUID userId, UUID orderId);

    OrderResponse getOrderByNumber(UUID userId, String orderNumber);

    PagedResponse<OrderListResponse> getUserOrders(
            UUID userId, OrderStatus status, int page, int size);

    OrderResponse cancelOrder(UUID userId, UUID orderId, String reason);

    // ─── Admin ──────────────────────────────────────────────
    PagedResponse<OrderListResponse> getAllOrders(
            OrderStatus status,
            String search,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            int size);

    OrderResponse getOrderDetailAdmin(UUID orderId);

    OrderResponse updateOrderStatus(UUID orderId, OrderStatus newStatus);
}
