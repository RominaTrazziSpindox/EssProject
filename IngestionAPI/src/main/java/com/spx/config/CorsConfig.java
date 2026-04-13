package com.spx.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Defines the CORS configuration used by the application.
 * This configuration allows the frontend running on localhost:3000
 * to call the CRM sync endpoint exposed by the Producer service.
 */
@Configuration
public class CorsConfig {

    /**
     * Creates the CORS configuration source used by the CorsFilter.
     * It defines:
     * - the allowed origin
     * - the allowed HTTP methods
     * - the allowed request headers
     * - the exposed response headers
     * - the cache duration for preflight requests
     */
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("POST", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Content-Type", "X-API-KEY"));

        // Expose response headers that may be useful for debugging and rate limit handling.
        configuration.setExposedHeaders(List.of(
                "Content-Type",
                "X-Rate-Limit-Limit",
                "X-Rate-Limit-Remaining",
                "Retry-After"
        ));

        // Credentials are not needed because authentication is handled through API key headers.
        configuration.setAllowCredentials(false);

        // Cache the preflight response for 1 hour.
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // Apply this CORS configuration only to the CRM API endpoints.
        source.registerCorsConfiguration("/api/v1/crm/**", configuration);

        return source;
    }
}