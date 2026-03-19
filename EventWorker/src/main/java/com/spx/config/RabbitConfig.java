package com.spx.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // Constructor injection (of Rabbit properties)
    private final RabbitProperties rabbitProperties;

    public RabbitConfig(RabbitProperties rabbitProperties) {
        this.rabbitProperties = rabbitProperties;
    }

    /**
     * Main Queue
     * Declares the queue used by the EventWorker consumer.
     * If the queue does not exist, RabbitMQ will create it automatically.
     */
    @Bean
    public Queue campaignQueue() {
        return QueueBuilder.durable(rabbitProperties.getQueue())
                .withArgument("x-dead-letter-exchange", rabbitProperties.getDlx())
                .withArgument("x-dead-letter-routing-key", rabbitProperties.getQueue())
                .build();
    }

    /**
     * Main Exchange
     * Where the Producer publishes messages
     */
    @Bean
    public DirectExchange crmExchange() {
        return new DirectExchange(rabbitProperties.getExchange());
    }

    /**
     * Binding between the main Exchange and main Queue.
     */
    @Bean
    public Binding campaignBinding() {
        return BindingBuilder
                .bind(campaignQueue())
                .to(crmExchange())
                .with(rabbitProperties.getRoutingKey());
    }

    // Secondary Queue //

    // Dead Letter Queue
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(rabbitProperties.getDlq()).build();
    }

    // Dead Letter Exchange
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(rabbitProperties.getDlx());
    }

    // Binding DLQ → DLX
    @Bean
    public Binding dlqBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(rabbitProperties.getQueue());
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
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory (ConnectionFactory connectionFactory, JacksonJsonMessageConverter messageConverter) {

        SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory = new SimpleRabbitListenerContainerFactory();

        // Set the connection as connectionFactory object
        rabbitListenerContainerFactory.setConnectionFactory(connectionFactory);

        // Convert the incoming JSON from Rabbit into DTO object
        rabbitListenerContainerFactory.setMessageConverter(messageConverter);

        // Use between 2 and 5 Consumer together
        rabbitListenerContainerFactory.setConcurrentConsumers(2);
        rabbitListenerContainerFactory.setMaxConcurrentConsumers(5);

        // Prevent message requeue after retries are exhausted → message goes to DLQ
        rabbitListenerContainerFactory.setDefaultRequeueRejected(false);

        return rabbitListenerContainerFactory;
    }
}