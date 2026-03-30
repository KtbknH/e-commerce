package com.yoteh.api.dto.response;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageResponse {

    private UUID id;
    private String imageUrl;
    private String altText;
    private Integer sortOrder;
    private Boolean isPrimary;
}
