package com.spx;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class EventWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventWorkerApplication.class, args);
        log.info("EventWorker app is running...");

    }
}