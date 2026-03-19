package com.spx;

import com.spx.config.RabbitProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RabbitProperties.class)
@Slf4j
public class IngestionAPIApplication {

    public static void main(String[] args) {
        SpringApplication.run(IngestionAPIApplication.class, args);
        log.info("IngestionAPI app is running...");
    }
}