package com.yoteh.api.dto.request;

import lombok.Data;

@Data
public class PaymentCallbackRequest {

    /** Identifiant externe chez le provider (Orange Money, MTN, etc.) */
    private String externalId;

    /** Référence interne Yoteh (YTH-PAY-...) */
    private String paymentReference;

    /** Statut retourné par le provider (SUCCESS, FAILED, PENDING, etc.) */
    private String status;

    /** ID de transaction du provider */
    private String transactionId;

    private String amount;
    private String currency;
    private String phoneNumber;

    /** Signature de vérification HMAC du provider */
    private String signature;
}
