package com.yoteh.api.service;

import com.yoteh.api.dto.request.AdjustPointsRequest;
import com.yoteh.api.dto.response.LoyaltyBalanceResponse;
import com.yoteh.api.dto.response.LoyaltyTransactionResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import java.math.BigDecimal;
import java.util.UUID;

public interface LoyaltyService {

    // ── Client ──

    LoyaltyBalanceResponse getMyBalance(UUID userId);

    PagedResponse<LoyaltyTransactionResponse> getMyHistory(
            UUID userId, int page, int size, String type);

    // ── Logique métier ──

    void earnPointsFromOrder(UUID userId, UUID orderId, BigDecimal orderTotal);

    void redeemPoints(UUID userId, int points, String description, String reference);

    // ── Admin ──

    PagedResponse<LoyaltyTransactionResponse> getAllTransactions(int page, int size, String type);

    LoyaltyTransactionResponse adjustPoints(UUID userId, AdjustPointsRequest request);

    LoyaltyBalanceResponse getUserBalance(UUID userId);
}
