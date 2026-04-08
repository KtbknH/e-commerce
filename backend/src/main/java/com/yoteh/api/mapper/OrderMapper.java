package com.yoteh.api.mapper;

import com.yoteh.api.dto.response.OrderItemResponse;
import com.yoteh.api.dto.response.OrderListResponse;
import com.yoteh.api.dto.response.OrderResponse;
import com.yoteh.api.entity.Order;
import com.yoteh.api.entity.OrderItem;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.fullName")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "itemCount", source = "items", qualifiedByName = "countItems")
    OrderResponse toOrderResponse(Order order);

    @Mapping(target = "userName", source = "user.fullName")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "itemCount", source = "items", qualifiedByName = "countItems")
    OrderListResponse toOrderListResponse(Order order);

    List<OrderListResponse> toOrderListResponseList(List<Order> orders);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "variantId", source = "variant.id")
    @Mapping(target = "status", source = "status")
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);

    List<OrderItemResponse> toOrderItemResponseList(List<OrderItem> orderItems);

    @Named("countItems")
    default Integer countItems(List<OrderItem> items) {
        return items != null ? items.size() : 0;
    }
}
