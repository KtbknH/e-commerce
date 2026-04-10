package com.yoteh.api.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatsResponse {

    // ── Résumé ──
    private long totalProducts;
    private long activeProducts;
    private long inactiveProducts;
    private long featuredProducts;
    private long lowStockProducts;
    private long outOfStockProducts;

    // ── Top produits vendus ──
    private List<TopProduct> topSellingProducts;

    // ── Top produits par chiffre d'affaires ──
    private List<TopProduct> topRevenueProducts;

    // ── Produits en stock critique ──
    private List<LowStockProduct> lowStockList;

    // ── Inner classes ──

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProduct {
        private String productName;
        private String productSku;
        private long quantitySold;
        private BigDecimal totalRevenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LowStockProduct {
        private String productName;
        private String productSku;
        private int currentStock;
        private int lowStockThreshold;
    }
}
