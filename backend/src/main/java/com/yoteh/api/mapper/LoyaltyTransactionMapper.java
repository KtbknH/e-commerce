package com.yoteh.api.mapper;

import com.yoteh.api.dto.response.LoyaltyTransactionResponse;
import com.yoteh.api.entity.LoyaltyTransaction;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoyaltyTransactionMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "userFullName", expression = "java(entity.getUser().getFullName())")
    @Mapping(source = "order.id", target = "orderId")
    LoyaltyTransactionResponse toResponse(LoyaltyTransaction entity);

    List<LoyaltyTransactionResponse> toResponseList(List<LoyaltyTransaction> entities);
}
