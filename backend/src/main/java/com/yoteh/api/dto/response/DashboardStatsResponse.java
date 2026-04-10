package com.yoteh.api.dto.response;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

  // ── Commandes ──
  private long totalOrders;
  private long pendingOrders;
  private long paidOrders;
  private long preparingOrders;
  private long shippedOrders;
  private long deliveredOrders;
  private long cancelledOrders;

  // ── Chiffre d'affaires ──
  private BigDecimal totalRevenue;
  private BigDecimal todayRevenue;
  private BigDecimal weekRevenue;
  private BigDecimal monthRevenue;
  private BigDecimal averageOrderValue;

  // ── Produits ──
  private long totalProducts;
  private long activeProducts;
  private long featuredProducts;
  private long lowStockProducts;
  private long outOfStockProducts;

  // ── Utilisateurs ──
  private long totalUsers;
  private long activeUsers;
  private long newUsersToday;
  private long newUsersThisWeek;
  private long newUsersThisMonth;

  // ── Commandes par statut (map détaillée) ──
  private Map<String, Long> ordersByStatus;
}