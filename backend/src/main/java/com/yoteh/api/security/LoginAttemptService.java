package com.yoteh.api.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Protection contre les attaques par force brute. Bloque une IP après un nombre configurable de
 * tentatives échouées.
 */
@Service
public class LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);

    @Value("${app.security.login.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.security.login.lockout-minutes:15}")
    private int lockoutMinutes;

    private final Map<String, AttemptInfo> attempts = new ConcurrentHashMap<>();

    /** Enregistrer une tentative échouée pour une IP. */
    public void loginFailed(String ip) {
        AttemptInfo info = attempts.computeIfAbsent(ip, k -> new AttemptInfo());
        long now = System.currentTimeMillis();

        // Reset si la fenêtre est expirée
        if (now - info.getFirstAttempt() > lockoutMinutes * 60 * 1000L) {
            info.reset(now);
        }

        int count = info.incrementAndGet();
        if (count >= maxAttempts) {
            log.warn("IP {} bloquée après {} tentatives de connexion échouées", ip, count);
        }
    }

    /** Réinitialiser le compteur après une connexion réussie. */
    public void loginSucceeded(String ip) {
        attempts.remove(ip);
    }

    /** Vérifier si une IP est bloquée. */
    public boolean isBlocked(String ip) {
        AttemptInfo info = attempts.get(ip);
        if (info == null) {
            return false;
        }

        long now = System.currentTimeMillis();
        // Débloquer si la période de lockout est expirée
        if (now - info.getFirstAttempt() > lockoutMinutes * 60 * 1000L) {
            attempts.remove(ip);
            return false;
        }

        return info.getCount() >= maxAttempts;
    }

    /** Nettoyage des entrées expirées. */
    public void cleanupExpiredEntries() {
        long now = System.currentTimeMillis();
        long expiryMillis = lockoutMinutes * 60 * 1000L * 2;
        attempts.entrySet()
                .removeIf(entry -> now - entry.getValue().getFirstAttempt() > expiryMillis);
    }

    // ── Inner class ──

    private static class AttemptInfo {
        private final AtomicLong firstAttempt = new AtomicLong(System.currentTimeMillis());
        private final AtomicInteger count = new AtomicInteger(0);

        public long getFirstAttempt() {
            return firstAttempt.get();
        }

        public int getCount() {
            return count.get();
        }

        public int incrementAndGet() {
            return count.incrementAndGet();
        }

        public void reset(long newStart) {
            firstAttempt.set(newStart);
            count.set(0);
        }
    }
}
