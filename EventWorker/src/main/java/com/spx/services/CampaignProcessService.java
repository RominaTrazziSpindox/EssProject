package com.spx.services;

import com.spx.dto.CampaignEventDTO;
import com.spx.models.Attendee;
import com.spx.models.Campaign;
import com.spx.mappers.AttendeeMapper;
import com.spx.mappers.CampaignMapper;
import com.spx.repos.CampaignRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CampaignProcessService {

    // Constructor injection
    private final CampaignRepository campaignRepository;
    private final CampaignMapper campaignMapper;
    private final AttendeeMapper attendeeMapper;

    public CampaignProcessService(CampaignRepository campaignRepository, CampaignMapper campaignMapper, AttendeeMapper attendeeMapper) {
        this.campaignRepository = campaignRepository;
        this.campaignMapper = campaignMapper;
        this.attendeeMapper = attendeeMapper;
    }

    /**
     * Processes an incoming campaign event coming from RabbitMQ.
     *
     * Responsibilities:
     * - ensure idempotent processing
     * - apply Full State Synchronization
     * - persist the campaign and its attendees
     *
     * Important design notes:
     *
     * 1) Idempotency
     * RabbitMQ guarantees "at least once delivery".
     * The same event may arrive multiple times.
     * Processing the same event repeatedly must produce the same final DB state.
     *
     * 2) Full State Sync
     * The CRM payload represents a snapshot of the campaign state.
     * The database state must match exactly the incoming attendee list.
     *
     * 3) Exception propagation
     * Exceptions are intentionally NOT caught here.
     * If a failure occurs, the RabbitMQ listener will retry the message and eventually route it to a Dead Letter Queue (DLQ).
     *
     */
    @Transactional
    public void processCampaignFromRabbit(CampaignEventDTO campaignEventDTO) {

        log.info("Processing campaign event - campaignId: {}, subCampaignId: {}", campaignEventDTO.getCampaignId(), campaignEventDTO.getSubCampaignId());

        // STEP 1: Try to retrieve a Campaign using the business key composed of: campaignId + subCampaignId (it retrieves: Optional.empty() or Optional<Campaign>)
        Optional<Campaign> optionalCampaign = campaignRepository.findByCampaignIdAndSubCampaignId(campaignEventDTO.getCampaignId(), campaignEventDTO.getSubCampaignId());

        // STEP 1.1: Create an Object type Campaign without assigning any properties yet.
        Campaign campaign;

        // STEP 2:
        if (optionalCampaign.isEmpty()) {

            // CASE 1 — If a Campaign does not exist
            log.info("Campaign not found. Creating a new campaign.");

            // Step 1.1: Create a new campaign object using CampaignMapper (from DTO -> to Entity\Java Object)
            campaign = campaignMapper.toEntity(campaignEventDTO);

        } else {

            // CASE 2 — Else a Campaign already exists
            log.info("Campaign already exist. Updating an existing one. Campaign: {}", campaignEventDTO.getCampaignId());

            // Step 2.1: Retrieve the existing campaign object from the database
            campaign = optionalCampaign.get();
        }

        /* STEP 3: Create a List of attendee using AttendeeMapper (from DTO -> to Entity\Java Object)
        Important: attendeeMapper doesn't know about foreign key, so the retrieved list is not linked to a Campaign */
        List<Attendee> attendees = attendeeMapper.toEntityList(campaignEventDTO.getAttendees());

        // STEP 4: Link each attendee to a specific Campaign (property: campaign_id_rif that is the foreign key of Attendee table)
        attendees.forEach(
                attendee -> attendee.setCampaign(campaign)
        );

        // Step 5: Link the entire List of attendees to a specific campaign (property: attendees of Campaign model)
        campaign.setAttendees(attendees);

        // Step 6: Persist the campaign and all its attendees together (due to CascadeType.ALL operations on Campaign entity are extended to Attendee entity)
        campaignRepository.save(campaign);

        log.info("Campaign processed successfully - campaignId: {}, subCampaignId: {}, attendees: {}", campaignEventDTO.getCampaignId(), campaignEventDTO.getSubCampaignId(), attendees.size());

    }
}