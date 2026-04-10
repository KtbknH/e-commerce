package com.yoteh.api.mapper;

import com.yoteh.api.dto.request.ShippingZoneRequest;
import com.yoteh.api.dto.response.ShippingZoneResponse;
import com.yoteh.api.entity.ShippingZone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ShippingZoneMapper {

    @Mapping(target = "estimatedDelivery", ignore = true)
    ShippingZoneResponse toResponse(ShippingZone zone);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    ShippingZone toEntity(ShippingZoneRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(ShippingZoneRequest request, @MappingTarget ShippingZone zone);
}
