package com.spx.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// Enables binding for custom mail-related application properties
@Configuration
@EnableConfigurationProperties(ReportEmailProperties.class)
public class MailConfig {
}