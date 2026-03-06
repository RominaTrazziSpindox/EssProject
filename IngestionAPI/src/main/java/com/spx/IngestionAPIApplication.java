package com.spx;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class IngestionAPIApplication {

    public static void main(String[] args) {
        SpringApplication.run(IngestionAPIApplication.class, args);
        log.info("IngestionAPI app is running...");
    }
}