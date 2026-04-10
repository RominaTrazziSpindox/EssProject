package com.spx.config;

import java.util.List;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("POST", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Content-Type", "X-API-KEY"));
        configuration.setExposedHeaders(List.of("Content-Type"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/v1/crm/**", configuration);

        return source;
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistrationBean(
            UrlBasedCorsConfigurationSource corsConfigurationSource) {

        CorsFilter corsFilter = new CorsFilter(corsConfigurationSource);

        FilterRegistrationBean<CorsFilter> registrationBean =
                new FilterRegistrationBean<>(corsFilter);

        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return registrationBean;
    }
}