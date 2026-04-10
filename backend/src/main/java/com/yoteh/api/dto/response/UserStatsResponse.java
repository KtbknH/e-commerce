package com.yoteh.api.dto.response;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsResponse {

  // ── Résumé ──
  private long totalUsers;
  private long activeUsers;
  private long inactiveUsers;
  private long verifiedUsers;
  private long unverifiedUsers;

  // ── Par rôle ──
  private Map<String, Long> usersByRole;

  // ── Par niveau de fidélité ──
  private Map<String, Long> usersByLoyaltyLevel;

  // ── Nouveaux inscrits par période ──
  private List<NewUsersStat> newUsersByDay;
  private List<NewUsersStat> newUsersByMonth;

  // ── Top clients ──
  private List<TopCustomer> topCustomers;

  // ── Inner classes ──

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class NewUsersStat {
    private String period;
    private long count;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TopCustomer {
    private String firstName;
    private String lastName;
    private String email;
    private long orderCount;
    private java.math.BigDecimal totalSpent;
  }
}