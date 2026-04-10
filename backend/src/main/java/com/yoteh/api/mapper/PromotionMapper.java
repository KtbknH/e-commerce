package com.yoteh.api.mapper;

import com.yoteh.api.dto.request.PromotionRequest;
import com.yoteh.api.dto.response.PromotionResponse;
import com.yoteh.api.entity.Promotion;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PromotionMapper {

    /**
     * Entity → Response.
     *
     * <p>Champs calculés / relationnels : - applicableCategoryId / Name : extraits de l'association
     * ManyToOne applicableCategory - applicableProductId / Name : extraits de l'association
     * ManyToOne applicableProduct - valid : délégué à la méthode utilitaire isValid() de l'entité
     */
    @Mapping(target = "applicableCategoryId", source = "applicableCategory.id")
    @Mapping(target = "applicableCategoryName", source = "applicableCategory.name")
    @Mapping(target = "applicableProductId", source = "applicableProduct.id")
    @Mapping(target = "applicableProductName", source = "applicableProduct.name")
    @Mapping(target = "valid", expression = "java(promotion.isValid())")
    PromotionResponse toResponse(Promotion promotion);

    /**
     * Request → Entity (création).
     *
     * <p>Les associations (applicableCategory, applicableProduct) sont ignorées ici et renseignées
     * manuellement dans le service après lookup en base.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "applicableCategory", ignore = true)
    @Mapping(target = "applicableProduct", ignore = true)
    @Mapping(target = "usedCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Promotion toEntity(PromotionRequest request);

    /**
     * Request → Entity (mise à jour partielle).
     *
     * <p>NullValuePropertyMappingStrategy.IGNORE préserve les valeurs existantes quand le champ de
     * la request est null.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "applicableCategory", ignore = true)
    @Mapping(target = "applicableProduct", ignore = true)
    @Mapping(target = "usedCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(PromotionRequest request, @MappingTarget Promotion promotion);
}
