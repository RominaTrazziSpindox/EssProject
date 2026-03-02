package com.spx.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class CampaignPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitConfigProperties rabbitConfigProperties;

    public CampaignPublisher(RabbitTemplate rabbitTemplate, RabbitConfigProperties rabbitConfigProperties) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitConfigProperties = rabbitConfigProperties;
    }

    public void publishCampaignEvent(Object event) {
        rabbitTemplate.convertAndSend(rabbitConfigProperties.exchange(), rabbitConfigProperties.routingKey(), event
        );
    }
}