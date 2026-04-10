package com.yoteh.api.mapper;

import com.yoteh.api.dto.response.PaymentListResponse;
import com.yoteh.api.dto.response.PaymentResponse;
import com.yoteh.api.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderNumber", source = "order.orderNumber")
    @Mapping(target = "userId", source = "user.id")
    PaymentResponse toResponse(Payment payment);

    @Mapping(target = "orderNumber", source = "order.orderNumber")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(
            target = "userFullName",
            expression =
                    "java(payment.getUser().getFirstName() + \" \" + payment.getUser().getLastName())")
    PaymentListResponse toListResponse(Payment payment);
}
