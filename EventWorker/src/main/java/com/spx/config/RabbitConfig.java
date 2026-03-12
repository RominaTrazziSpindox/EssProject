package com.spx.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // Inject the Queue's name from application.yaml. It must be equal to the one into Producer
    @Value("${rabbitmq.queues.campaign}")
    private String campaignQueue;

    /**
     * Declares the queue used by the EventWorker consumer.
     * If the queue does not exist, RabbitMQ will create it automatically.
     */
    @Bean
    public Queue campaignQueue() {
        return new Queue(campaignQueue, true);
    }


    // -------- JSON Converter: converter used to deserialize JSON messages coming from RabbitMQ into DTO objects. --------
    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    /**
     * Rabbit listener container factory create a RabbitListener container factory object
     * used by @RabbitListener. It defines the configuration about how messages are consumed.
     *
     * @param connectionFactory the connection factory
     * @param messageConverter  the message converter
     * @return the simple rabbit listener container factory
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactoryCreator (ConnectionFactory connectionFactory, JacksonJsonMessageConverter messageConverter) {

        SimpleRabbitListenerContainerFactory rabbitListenerFactory = new SimpleRabbitListenerContainerFactory();

        // Set the connection as connectionFactory object
        rabbitListenerFactory.setConnectionFactory(connectionFactory);

        // Convert the incoming JSON from Rabbit into DTO object
        rabbitListenerFactory.setMessageConverter(messageConverter);

        // Use between 2 and 5 Consumer togheter
        rabbitListenerFactory.setConcurrentConsumers(2);
        rabbitListenerFactory.setMaxConcurrentConsumers(5);

        return rabbitListenerFactory;
    }
}