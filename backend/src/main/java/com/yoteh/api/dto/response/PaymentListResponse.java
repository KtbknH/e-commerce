package com.yoteh.api.dto.response;

import com.yoteh.api.entity.enums.PaymentMethod;
import com.yoteh.api.entity.enums.PaymentProvider;
import com.yoteh.api.entity.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class PaymentListResponse {

    private UUID id;
    private String paymentReference;

    private String orderNumber;
    private String userEmail;
    private String userFullName;

    private PaymentMethod method;
    private PaymentProvider provider;

    private PaymentStatus status;
    private BigDecimal amount;
    private String currency;

    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
