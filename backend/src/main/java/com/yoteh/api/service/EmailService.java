package com.yoteh.api.service;

import java.util.Map;

/** Service d'envoi d'emails. Gère l'envoi SMTP brut (texte ou HTML). */
public interface EmailService {

    /** Envoie un email simple (texte brut). */
    void sendSimpleEmail(String to, String subject, String text);

    /**
     * Envoie un email HTML à partir d'un template Thymeleaf.
     *
     * @param to adresse destinataire
     * @param subject sujet de l'email
     * @param templateName nom du template (sans extension, ex: "welcome")
     * @param variables variables injectées dans le template
     */
    void sendHtmlEmail(
            String to, String subject, String templateName, Map<String, Object> variables);
}
