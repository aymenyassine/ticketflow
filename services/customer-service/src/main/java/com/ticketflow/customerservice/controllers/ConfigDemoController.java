package com.ticketflow.customerservice.controllers;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class ConfigDemoController {
    private final String message;

    public ConfigDemoController(@Value("${ticketflow.message}") String message) {
        this.message = message;
    }

        @GetMapping("/api/config/message")
    public Map<String, String> message() {
        return Map.of("message", message);
    }
}
