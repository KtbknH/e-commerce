package com.yoteh.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class YotehApplication {

    private static final Logger log = LoggerFactory.getLogger(YotehApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(YotehApplication.class, args);
        log.info("=============================================");
        log.info("   YOTEH BACKEND API STARTED SUCCESSFULLY");
        log.info("   Health:  http://localhost:8080/health");
        log.info("   Swagger: http://localhost:8080/swagger-ui.html");
        log.info("=============================================");
    }
}
