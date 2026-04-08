package com.yoteh.api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private UUID id;
    private String orderNumber;
    private UUID userId;
    private String userName;
    private String userEmail;
    private String status;
    private List<OrderItemResponse> items;
    private Integer itemCount;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal shippingAmount;
    private BigDecimal taxAmount;
    private BigDecimal total;
    private String currency;
    private String promotionCode;

    // Adresse de livraison (snapshot)
    private String shippingFirstName;
    private String shippingLastName;
    private String shippingPhone;
    private String shippingStreet;
    private String shippingCity;
    private String shippingState;
    private String shippingPostalCode;
    private String shippingCountry;

    private String customerNote;
    private String trackingNumber;
    private String cancelReason;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
