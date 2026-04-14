package com.yoteh.api.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Set;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Intercepteur qui détermine la locale de la requête.
 *
 * <p>Ordre de priorité :
 *
 * <ol>
 *   <li>Paramètre de requête {@code ?lang=en}
 *   <li>Header HTTP {@code Accept-Language}
 *   <li>Fallback : {@code fr} (français)
 * </ol>
 */
public class LocaleInterceptor implements HandlerInterceptor {

    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("fr", "en");
    private static final Locale DEFAULT_LOCALE = Locale.FRENCH;

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler) {

        Locale locale = resolveLocale(request);
        LocaleContextHolder.setLocale(locale);
        return true;
    }

    private Locale resolveLocale(HttpServletRequest request) {
        // 1. Paramètre ?lang=
        String langParam = request.getParameter("lang");
        if (langParam != null && SUPPORTED_LANGUAGES.contains(langParam.toLowerCase())) {
            return Locale.forLanguageTag(langParam.toLowerCase());
        }

        // 2. Header Accept-Language
        String acceptLanguage = request.getHeader("Accept-Language");
        if (acceptLanguage != null && !acceptLanguage.isBlank()) {
            String lang = acceptLanguage.split("[,;_-]")[0].trim().toLowerCase();
            if (SUPPORTED_LANGUAGES.contains(lang)) {
                return Locale.forLanguageTag(lang);
            }
        }

        // 3. Fallback
        return DEFAULT_LOCALE;
    }
}
