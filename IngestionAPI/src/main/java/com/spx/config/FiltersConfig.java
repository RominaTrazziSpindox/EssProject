package com.spx.config;

import com.spx.security.ApiKeyFilter;
import com.spx.security.ApiKeyRateLimitFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Registers the servlet filters used by the application.
 * The filters are ordered explicitly to ensure that CORS is handled first,
 * followed by API key validation and then rate limiting.
 */
@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class FiltersConfig {

    /**
     * Registers the CORS filter.
     * This filter must run before the other filters in order to handle
     * preflight requests and add the required CORS headers.
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistrationBean(UrlBasedCorsConfigurationSource corsConfigurationSource) {
        CorsFilter corsFilter = new CorsFilter(corsConfigurationSource);
        FilterRegistrationBean<CorsFilter> registrationBean = new FilterRegistrationBean<>(corsFilter);
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }

    /**
     * Registers the API key validation filter.
     * This filter runs after the CORS filter and blocks unauthorized requests
     * with HTTP 401 before they reach the controller.
     */
    @Bean
    public FilterRegistrationBean<ApiKeyFilter> apiKeyFilterRegistration(ApiKeyFilter filter) {
        FilterRegistrationBean<ApiKeyFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/api/v1/crm/sync");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registration;
    }

    /**
     * Registers the rate limiting filter.
     * This filter runs after the API key validation filter and blocks
     * excessive requests with HTTP 429.
     */
    @Bean
    public FilterRegistrationBean<ApiKeyRateLimitFilter> apiKeyRateLimitFilterRegistration(ApiKeyRateLimitFilter filter) {
        FilterRegistrationBean<ApiKeyRateLimitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/api/v1/crm/sync");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
        return registration;
    }
}