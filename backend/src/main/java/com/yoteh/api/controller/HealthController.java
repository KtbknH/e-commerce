package com.yoteh.api.controller;

import com.yoteh.api.dto.response.common.ApiResponse;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @Value("${spring.application.name}")
    private String appName;

    @Value("${yoteh.default-currency}")
    private String defaultCurrency;

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> data =
                Map.of(
                        "app",
                        appName,
                        "status",
                        "UP",
                        "version",
                        "0.1.0",
                        "currency",
                        defaultCurrency,
                        "timestamp",
                        LocalDateTime.now().toString());
        return ResponseEntity.ok(ApiResponse.ok("Yoteh API is running", data));
    }
}
