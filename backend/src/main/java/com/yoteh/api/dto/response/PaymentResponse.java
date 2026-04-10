package com.yoteh.api.dto.response;

import com.yoteh.api.entity.enums.PaymentMethod;
import com.yoteh.api.entity.enums.PaymentProvider;
import com.yoteh.api.entity.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class PaymentResponse {

    private UUID id;
    private String paymentReference;

    private UUID orderId;
    private String orderNumber;

    private UUID userId;

    private PaymentMethod method;
    private PaymentProvider provider;
    private String phoneNumber;

    private PaymentStatus status;
    private BigDecimal amount;
    private String currency;

    /** URL de paiement externe (page Orange Money, MTN, etc.) */
    private String externalId;

    private String externalUrl;

    private String errorCode;
    private String errorMessage;

    private String refundReason;
    private BigDecimal refundedAmount;

    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
