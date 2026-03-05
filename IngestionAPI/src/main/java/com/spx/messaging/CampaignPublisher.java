package com.spx.messaging;

import com.spx.config.RabbitConfigProperties;
import com.spx.dto.CrmIncomingCampaignDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class CampaignPublisher {

    // Constructor injection
    private final RabbitTemplate rabbitTemplate;
    private final RabbitConfigProperties rabbitConfigProperties;

    public CampaignPublisher(RabbitTemplate rabbitTemplate, RabbitConfigProperties rabbitConfigProperties) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitConfigProperties = rabbitConfigProperties;
    }

    /* Method that receives a single campaign at a time of DTO type, then convert it in JSON format and
     send it to the configured exchange (TopicExchange) using the routing key defined in application.yaml. */
    public void publishCampaign(CrmIncomingCampaignDTO singleCampaignDTO) {
        rabbitTemplate.convertAndSend(
                rabbitConfigProperties.exchange(),
                rabbitConfigProperties.routingKey(),
                singleCampaignDTO
        );
    }
}