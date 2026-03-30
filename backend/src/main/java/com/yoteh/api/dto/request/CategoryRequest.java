package com.yoteh.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "Le nom de la catégorie est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String name;

    @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
    private String description;

    private String imageUrl;

    private UUID parentId;

    @Size(max = 50, message = "Le type ne doit pas dépasser 50 caractères")
    private String type;

    private Integer sortOrder;

    private Boolean isActive;
}
