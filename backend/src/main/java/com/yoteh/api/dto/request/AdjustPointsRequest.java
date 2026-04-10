package com.yoteh.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdjustPointsRequest {

    @NotNull(message = "Le nombre de points est obligatoire")
    private Integer points;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    private String reference;
}
