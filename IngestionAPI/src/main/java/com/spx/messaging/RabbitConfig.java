package com.spx.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableConfigurationProperties(RabbitConfigProperties.class)
public class RabbitConfig {

    // -------- JSON Converter --------

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter(JsonMapper jsonMapper) {
        return new JacksonJsonMessageConverter(jsonMapper);
    }

    // -------- RabbitTemplate --------

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            JacksonJsonMessageConverter converter) {

        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

    // -------- Exchange --------

    @Bean
    public TopicExchange topicExchange(RabbitConfigProperties properties) {
        return new TopicExchange(
                properties.exchange(),
                true,   // durable
                false   // autoDelete
        );
    }

    // -------- Queue --------

    @Bean
    public Queue campaignQueue(RabbitConfigProperties properties) {
        return new Queue(
                properties.queue(),
                true   // durable
        );
    }

    // -------- Binding --------

    @Bean
    public Binding binding(
            Queue campaignQueue,
            TopicExchange topicExchange,
            RabbitConfigProperties properties) {

        return BindingBuilder
                .bind(campaignQueue)
                .to(topicExchange)
                .with(properties.routingKey());
    }
}