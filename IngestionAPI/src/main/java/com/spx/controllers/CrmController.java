package com.spx.controllers;

import com.spx.dto.CrmIncomingCampaignDTO;
import com.spx.dto.CrmSyncResponseDTO;
import com.spx.services.CrmSyncService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api")
@Validated
public class CrmController {

    // Constructor injection
    private final CrmSyncService crmSyncService;

    public CrmController(CrmSyncService crmSyncService) {
        this.crmSyncService = crmSyncService;
    }

    String batchId = UUID.randomUUID().toString().substring(0,8);

    /* It receives the list of the incoming campaigns + attendees from the CRM (in DTO format)
    and retrieve a Void entity + a 202 Accepted HTTP status code */
    @PostMapping("/v1/crm/sync")
    public ResponseEntity<CrmSyncResponseDTO> syncCampaigns(@Valid @RequestBody List<@Valid CrmIncomingCampaignDTO> campaigns) {

        // Call the method which split every campaign in a single message for RabbitTemplate
        crmSyncService.processBatch(campaigns);

        CrmSyncResponseDTO response = CrmSyncResponseDTO.builder()
                .status("ACCEPTED")
                .batchId(batchId)
                .campaignsReceived(campaigns.size())
                .message("Campaign batch accepted for processing")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.accepted().body(response);
    }
}
