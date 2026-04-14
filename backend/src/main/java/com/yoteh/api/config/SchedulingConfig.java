package com.yoteh.api.config;

import com.yoteh.api.security.RateLimitingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@EnableAsync
public class SchedulingConfig {

    private static final Logger log = LoggerFactory.getLogger(SchedulingConfig.class);

    private final RateLimitingFilter rateLimitingFilter;

    public SchedulingConfig(RateLimitingFilter rateLimitingFilter) {
        this.rateLimitingFilter = rateLimitingFilter;
    }

    /** Nettoyage des entrées rate-limit expirées toutes les 5 minutes. */
    @Scheduled(fixedRate = 300000)
    public void cleanupRateLimitEntries() {
        rateLimitingFilter.cleanupExpiredEntries();
        log.debug("Nettoyage des entrées rate-limit effectué");
    }
}
