package com.yoteh.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Levée quand une opération viole une règle métier (ex : modifier l'avis d'un autre utilisateur,
 * annuler une commande déjà livrée...). Mapped → HTTP 400 Bad Request.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
