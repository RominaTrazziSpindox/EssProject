package com.spx.config;

import java.time.Duration;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.rate-limit")
@Validated
@Getter
@Setter
public class RateLimitProperties {

    // Properties of the filter: capacity and window

    @Positive
    private long capacity = 5;

    @NotNull
    private Duration window = Duration.ofMinutes(1);
}