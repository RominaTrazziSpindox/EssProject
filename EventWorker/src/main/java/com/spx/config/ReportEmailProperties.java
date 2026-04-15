package com.spx.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

// Configuration properties for report reporting delivery. All the properties are taken from application.yaml
@ConfigurationProperties(prefix = "app.report.email")
@Getter
@Setter
public class ReportEmailProperties {

    private String from;
    private String to;
    private String subject;

}