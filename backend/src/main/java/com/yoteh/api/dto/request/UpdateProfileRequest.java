package com.yoteh.api.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(min = 2, max = 100, message = "Le prénom doit contenir entre 2 et 100 caractères")
    @Pattern(
            regexp = "^[a-zA-ZÀ-ÿ\\s'-]+$",
            message = "Le prénom contient des caractères invalides")
    private String firstName;

    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s'-]+$", message = "Le nom contient des caractères invalides")
    private String lastName;

    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Le numéro de téléphone est invalide")
    private String phone;

    @Size(max = 500, message = "L'URL de l'avatar est trop longue")
    private String avatar;

    @Size(max = 5, message = "Le code langue est trop long")
    private String preferredLanguage;

    @Size(max = 3, message = "Le code devise est trop long")
    private String preferredCurrency;
}
