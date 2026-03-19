package com.spx;

import com.spx.config.RabbitProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RabbitProperties.class)
@Slf4j
public class EventWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventWorkerApplication.class, args);
        log.info("EventWorker app is running...");

    }
}