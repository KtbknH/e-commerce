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
public class SalesStatsResponse {

  // ── Résumé ──
  private BigDecimal totalRevenue;
  private long totalOrders;
  private BigDecimal averageOrderValue;
  private BigDecimal totalDiscount;
  private BigDecimal totalShipping;

  // ── Revenus par période ──
  private List<PeriodRevenue> revenueByDay;
  private List<PeriodRevenue> revenueByMonth;

  // ── Paiements par méthode ──
  private List<PaymentMethodStat> paymentsByMethod;

  // ── Inner classes ──

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PeriodRevenue {
    private String period;
    private BigDecimal revenue;
    private long orderCount;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PaymentMethodStat {
    private String method;
    private long count;
    private BigDecimal totalAmount;
  }
}