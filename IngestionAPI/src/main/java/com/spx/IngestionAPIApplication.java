package com.spx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IngestionAPIApplication {

    public static void main(String[] args) {
        SpringApplication.run(IngestionAPIApplication.class, args);
        System.out.println("IngestionAPI app is running...");
    }
}