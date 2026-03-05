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
     * 3) Track how many campaigns were successfully published and how many failed
     *
     * Important:
     *
     * Error handling strategy:
     *
     * If RabbitMQ is unavailable (AmqpException), the error is considered
     * a critical infrastructure failure. The exception is rethrown so that
     * the GlobalExceptionHandler can return HTTP 503.
     *
     * If an error occurs while processing a single campaign (for example
     * IllegalArgumentException or other unexpected runtime exceptions),
     * the failure is logged and the batch continues with the next campaign.
     * This prevents a single bad item from blocking the whole batch.
     *
     */

    public void processBatch(List<CrmIncomingCampaignDTO> campaigns) {

        // Batch start log
        log.info("Received {} campaigns from CRM", campaigns.size());

        int published = 0;
        int failed = 0;

        for (CrmIncomingCampaignDTO campaign : campaigns) {

            // Publish campaign event to RabbitMQ
            try {
                publisher.publishCampaign(campaign);
                published++;

                /* Infrastructure failure: RabbitMQ not reachable or connection lost
                The exception is rethrown so the API returns HTTP 503 */
            } catch (org.springframework.amqp.AmqpException ex) {
                log.error("RabbitMQ unavailable while publishing campaign {}", campaign.getCampaignId(), ex);
                throw ex;

                /* Non-critical error on a single campaign
                The batch continues processing the remaining campaigns */
            } catch (Exception ex) {
                failed++;
                log.error("Error publishing campaign {}", campaign.getCampaignId(), ex);
            }
        }

        // Batch summary log with results
        log.info("Campaign batch processed: total={}, published={}, failed={}", campaigns.size(), published, failed);

    }
}

