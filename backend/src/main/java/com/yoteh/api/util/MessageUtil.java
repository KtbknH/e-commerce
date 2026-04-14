package com.yoteh.api.util;

import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utilitaire centralisant la résolution des messages internationalisés.
 *
 * <p>Utilisation :
 *
 * <pre>
 *   messageUtil.get("error.product.not_found")           → locale courante
 *   messageUtil.get("error.product.not_found", "123")    → avec argument
 *   messageUtil.get("error.product.not_found", locale)   → locale spécifique
 * </pre>
 */
@Component
public class MessageUtil {

    private final MessageSource messageSource;

    public MessageUtil(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /** Résout un message avec la locale courante (depuis Accept-Language ou ?lang=). */
    public String get(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

    /** Résout un message avec des arguments et la locale courante. */
    public String get(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }

    /** Résout un message avec une locale spécifique (ex: langue préférée de l'utilisateur). */
    public String getForLocale(String code, Locale locale) {
        return messageSource.getMessage(code, null, code, locale);
    }

    /** Résout un message avec arguments et locale spécifique. */
    public String getForLocale(String code, Locale locale, Object... args) {
        return messageSource.getMessage(code, args, locale);
    }

    /** Résout un message pour la langue préférée d'un utilisateur (ex: "fr", "en"). */
    public String getForLanguage(String code, String language) {
        Locale locale = Locale.forLanguageTag(language != null ? language : "fr");
        return messageSource.getMessage(code, null, locale);
    }

    /** Résout un message avec arguments pour la langue préférée d'un utilisateur. */
    public String getForLanguage(String code, String language, Object... args) {
        Locale locale = Locale.forLanguageTag(language != null ? language : "fr");
        return messageSource.getMessage(code, args, locale);
    }
}
