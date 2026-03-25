package com.yoteh.api.dto.response;

import com.yoteh.api.entity.enums.LoyaltyLevel;
import com.yoteh.api.entity.enums.UserRole;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String avatar;
    private UserRole role;
    private Boolean isActive;
    private Boolean isVerified;
    private LocalDateTime lastLogin;

    // Fidélité
    private Integer loyaltyPoints;
    private LoyaltyLevel loyaltyLevel;

    // Préférences
    private String preferredLanguage;
    private String preferredCurrency;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Compteurs (remplis côté admin)
    private Long orderCount;
    private Long addressCount;
}
