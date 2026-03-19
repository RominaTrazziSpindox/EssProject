package com.spx.messaging;

import com.spx.config.RabbitProperties;
import com.spx.dto.CrmIncomingCampaignDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class CampaignPublisher {

    // Constructor injection
    private final RabbitTemplate rabbitTemplate;
    private final RabbitProperties rabbitProperties;

    public CampaignPublisher(RabbitTemplate rabbitTemplate, RabbitProperties rabbitProperties) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitProperties = rabbitProperties;
    }

    /* Method that receives a single campaign at a time of DTO type, then convert it in JSON format and
     send it to the configured exchange (TopicExchange) using the routing key defined in application.yaml. */
    public void publishCampaign(CrmIncomingCampaignDTO singleCampaignDTO) {
        rabbitTemplate.convertAndSend(
                rabbitProperties.getExchange(),
                rabbitProperties.getRoutingKey(),
                singleCampaignDTO
        );
    }
}