package com.spx.messaging;
import org.springframework.boot.context.properties.ConfigurationProperties;

// It takes the app.properties from the application.yaml file and insert them into a new Java object
@ConfigurationProperties(prefix = "app.rabbit")
public record RabbitConfigProperties(String exchange, String routingKey, String queue) {

}
