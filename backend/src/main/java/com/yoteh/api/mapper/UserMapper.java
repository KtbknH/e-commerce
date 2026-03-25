package com.yoteh.api.mapper;

import com.yoteh.api.dto.response.UserResponse;
import com.yoteh.api.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "orderCount", ignore = true)
    @Mapping(target = "addressCount", ignore = true)
    UserResponse toResponse(User user);

    default UserResponse toResponseWithCounts(User user, Long orderCount, Long addressCount) {
        UserResponse response = toResponse(user);
        response.setOrderCount(orderCount);
        response.setAddressCount(addressCount);
        return response;
    }
}
