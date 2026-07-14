package com.example.ppm.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppConfigCorsTest {

    @Test
    void allowsTheProductionSiteAsCorsOrigin() {
        TestCorsRegistry registry = new TestCorsRegistry();

        new AppConfig().addCorsMappings(registry);

        assertEquals("https://projectforge.868601.xyz",
                registry.mappings().get("/api/**")
                        .checkOrigin("https://projectforge.868601.xyz"));
    }

    private static class TestCorsRegistry extends CorsRegistry {
        Map<String, org.springframework.web.cors.CorsConfiguration> mappings() {
            return getCorsConfigurations();
        }
    }
}
