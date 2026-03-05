package com.spx.services;

import com.spx.dto.CrmIncomingCampaignDTO;
import com.spx.messaging.CampaignPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.UUID;

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
     *
     * Processing flow:
     * 1) The request payload has already been validated by Controller layer using @Valid
     * 2) Each campaign is published as an independent message to RabbitMQ
     * 3) The method does not perform per-campaign validation or recovery logic
     *
     * Error handling:
     *
     * If RabbitMQ becomes unavailable during publishing (AmqpException),
     * the exception is propagated so the API layer can return HTTP 503.
     *
     * Since request validation happens before this service is called,
     * all campaigns in the list are considered structurally valid.
     */

    public void processBatch(List<CrmIncomingCampaignDTO> campaigns) {

        int totalCampaigns = campaigns.size();
        int published = 0;

        String batchId = UUID.randomUUID().toString().substring(0,8);

        log.info("[batch:{}] Processing {} campaigns", batchId, totalCampaigns);

        // For loop split the batch in single campaigns
        for (CrmIncomingCampaignDTO campaign : campaigns) {

            publisher.publishCampaign(campaign);
            published++;

            log.debug("[batch:{}] Campaign {} published", batchId, campaign.getCampaignId());
        }

        // Batch summary log with results
        log.info("[batch:{}] Batch completed: published={}", batchId, published);
    }
}

