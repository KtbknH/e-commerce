package com.yoteh.api.mapper;

import com.yoteh.api.dto.request.CategoryRequest;
import com.yoteh.api.dto.response.CategoryResponse;
import com.yoteh.api.entity.Category;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategoryMapper {

    // ─── Entity → Response (sans enfants, sans comptage) ────
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "productCount", ignore = true)
    CategoryResponse toResponse(Category category);

    // ─── Request → Entity (création) ───────────────────────
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category toEntity(CategoryRequest request);

    // ─── Request → Entity (mise à jour) ────────────────────
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(CategoryRequest request, @MappingTarget Category category);

    // ─── Méthode manuelle : arbre hiérarchique ─────────────
    default CategoryResponse toTreeResponse(Category category) {
        if (category == null) {
            return null;
        }

        CategoryResponse response = toResponse(category);

        // Compter les produits
        response.setProductCount(
                category.getProducts() != null ? category.getProducts().size() : 0);

        // Récursion sur les enfants
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            List<CategoryResponse> childResponses =
                    category.getChildren().stream()
                            .filter(Category::getIsActive)
                            .sorted(
                                    (a, b) -> {
                                        int sa = a.getSortOrder() != null ? a.getSortOrder() : 0;
                                        int sb = b.getSortOrder() != null ? b.getSortOrder() : 0;
                                        return Integer.compare(sa, sb);
                                    })
                            .map(this::toTreeResponse)
                            .collect(Collectors.toList());
            response.setChildren(childResponses);
        } else {
            response.setChildren(Collections.emptyList());
        }

        return response;
    }

    // ─── Liste plate ───────────────────────────────────────
    default List<CategoryResponse> toResponseList(List<Category> categories) {
        if (categories == null) {
            return Collections.emptyList();
        }
        return categories.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
