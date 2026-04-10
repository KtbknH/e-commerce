package com.yoteh.api.dto.request;

import com.yoteh.api.entity.enums.PaymentMethod;
import com.yoteh.api.entity.enums.PaymentProvider;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;
import lombok.Data;

@Data
public class InitPaymentRequest {

    @NotNull(message = "L'ID de la commande est obligatoire")
    private UUID orderId;

    @NotNull(message = "La méthode de paiement est obligatoire")
    private PaymentMethod method;

    /** Obligatoire si method == MOBILE_MONEY */
    private PaymentProvider provider;

    /** Obligatoire si method == MOBILE_MONEY */
    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Numéro de téléphone invalide")
    private String phoneNumber;

    private String currency = "XOF";

    /** URL de retour après paiement par carte */
    private String returnUrl;

    /** URL d'annulation pour les paiements par carte */
    private String cancelUrl;
}
