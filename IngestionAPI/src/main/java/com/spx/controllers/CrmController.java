package com.spx.controllers;

import com.spx.dto.CrmIncomingCampaignDTO;
import com.spx.dto.CrmSyncResponseDTO;
import com.spx.services.CrmSyncService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("api")
public class CrmController {

    // Constructor injection
    private final CrmSyncService crmSyncService;

    public CrmController(CrmSyncService crmSyncService) {
        this.crmSyncService = crmSyncService;
    }

    /* It receives the list of the incoming campaigns + attendees from the CRM (in DTO format)
    and retrieve a Void entity + a 202 Accepted HTTP status code */
    @PostMapping("/v1/crm/sync")
    public ResponseEntity<CrmSyncResponseDTO> syncCampaigns(@RequestBody List<@Valid CrmIncomingCampaignDTO> campaigns) {

        // Call the method which split every campaign in a single message for RabbitTemplate
        crmSyncService.processBatch(campaigns);

        CrmSyncResponseDTO response = CrmSyncResponseDTO.builder()
                .status("ACCEPTED")
                .campaignsReceived(campaigns.size())
                .message("Campaign batch accepted for processing")
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.accepted().body(response);
    }
}
