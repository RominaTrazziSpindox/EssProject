package com.spx.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "app.rabbit")
public class RabbitProperties {

    @NotBlank
    private String exchange;

    @NotBlank
    private String routingKey;

}