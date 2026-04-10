package com.yoteh.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyBalanceResponse {

    private Integer currentPoints;
    private String loyaltyLevel;
    private Integer totalEarned;
    private Integer totalRedeemed;
    private Integer pointsToNextLevel;
    private String nextLevel;
}
