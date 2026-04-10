package com.yoteh.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminReviewRequest {

    @NotNull(message = "La décision d'approbation est obligatoire")
    private Boolean approved;

    @Size(max = 1000, message = "La réponse admin ne doit pas dépasser 1000 caractères")
    private String adminResponse;
}
