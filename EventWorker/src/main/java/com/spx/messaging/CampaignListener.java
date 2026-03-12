package com.spx.messaging;

import com.spx.dto.CampaignEventDTO;
import com.spx.services.CampaignProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CampaignListener {

    // Constructor injection of service layer
    private final CampaignProcessService campaignProcessService;

    public CampaignListener(CampaignProcessService campaignProcessService) {
        this.campaignProcessService = campaignProcessService;
    }

    /**
     * RabbitMQ consumer.
     *
     * This listener is triggered every time a message arrives on the crm.campaigns.queue.
     * The payload is automatically deserialized into CampaignEventDTO from JSON thanks to the Jackson message converter configured in RabbitConfig.
     */
    @RabbitListener(queues = {"${app.rabbit.queue}"})
    public void consumeCampaign(CampaignEventDTO campaignEventDTO) {

        log.info( "Received campaign event from queue - campaignId={}, subCampaignId={}", campaignEventDTO.getCampaignId(), campaignEventDTO.getSubCampaignId());

        // Call service layer and its method
        campaignProcessService.processCampaignFromRabbit(campaignEventDTO);
    }
}