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


    /*
     * Batch orchestrator method.
     *
     * This method receives a list of campaigns coming from the CRM payload.
     * The batch is processed sequentially:
     *
     * 1) Iterate over the list
     * 2) For each campaign, publish an event to RabbitMQ
     * 3) Track how many campaigns succeed or fail
     *
     * Important: a failure on a single campaign MUST NOT stop the whole batch.
     * This is typical defensive behavior in event-driven systems.
     */
    public void processBatch(List<CrmIncomingCampaignDTO> campaigns) {

        log.info("Received {} campaigns from CRM", campaigns.size());

        int published = 0;
        int failed = 0;


        for (CrmIncomingCampaignDTO campaign : campaigns) {

            try {
                publisher.publishCampaign(campaign);
                published++;
            } catch (Exception ex) {
                failed++;
                log.error("Error publishing campaign {}", campaign.getCampaignId(), ex);
            }
        }

        // Batch summary log with results
        log.info("Campaign batch processed: total={}, published={}, failed={}", campaigns.size(), published, failed);
    }
}

