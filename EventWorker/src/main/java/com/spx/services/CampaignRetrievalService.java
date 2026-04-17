package com.spx.services;

import com.spx.dto.CampaignReportDTO;
import com.spx.mappers.CampaignReportMapper;
import com.spx.models.Campaign;
import com.spx.repos.CampaignRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/* Service responsible for retrieving all campaigns with their related attendees from the database,
converts them into CampaignReportDTO objects, and provides the report-ready data used by the aggregation
and Excel generation services. */

@Service
@Slf4j
public class CampaignRetrievalService {

    // Constants
    private final CampaignRepository campaignRepository;
    private final CampaignReportMapper campaignReportMapper;

    // Constructor
    public CampaignRetrievalService(CampaignRepository campaignRepository, CampaignReportMapper campaignReportMapper) {
        this.campaignRepository = campaignRepository;
        this.campaignReportMapper = campaignReportMapper;
    }

    /**
     * Loads all campaigns with attendees and maps them to report-specific DTOs.
     *
     * @return all campaign sections used by the reporting flow
     */
    @Transactional(readOnly = true)
    public List<CampaignReportDTO> getAllCampaignsForReport() {

        // Step 1: Find campaigns
        List<Campaign> campaigns = campaignRepository.findAllByOrderByCampaignIdAsc();

        // Step 2: Transform List<Campaign> Java Object -> DTO
        return campaigns
                .stream()
                .map(campaignReportMapper::toReportSection)
                .toList();
    }
}

