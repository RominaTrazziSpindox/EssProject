package com.spx.services;

import com.spx.dto.CrmIncomingCampaignDTO;
import com.spx.messaging.CampaignPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CrmSyncService {

    // Constructor injection
    private final CampaignPublisher publisher;

    public CrmSyncService(CampaignPublisher publisher) {
        this.publisher = publisher;
    }

    /* Method that receives a List of incoming campaigns and splits them in a single campaigns
    with for loop, then call the Producer class of RabbitMQ */
    public void splittingCampaigns(List<CrmIncomingCampaignDTO> campaigns) {

        log.info("Processing {} campaigns from CRM", campaigns.size());

        for (CrmIncomingCampaignDTO campaign : campaigns) {
            log.info("Publishing campaign {}", campaign.getCampaignId());
            publisher.publishCampaign(campaign);
        }
    }
}

