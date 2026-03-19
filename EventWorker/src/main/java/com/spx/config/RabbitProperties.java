package com.spx.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "app.rabbit")
public class RabbitProperties {

    // All the values are taken from application.yaml

    @NotBlank
    private String queue;

    @NotBlank
    private String exchange;

    @NotBlank
    private String routingKey;

    @NotBlank
    private String dlq;

    @NotBlank
    private String dlx;
}