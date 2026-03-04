package com.spx.controllers;

import com.spx.dto.CrmIncomingCampaignDTO;
import com.spx.services.CrmSyncService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api")
public class CrmController {

    private final CrmSyncService crmSyncService;

    public CrmController(CrmSyncService crmSyncService) {
        this.crmSyncService = crmSyncService;
    }

    /* It receives the list of the incoming campaingns + attendees from the CRM (in DTO format)
    and retrieve a Void entity + a 202 Accepted HTTP status code */
    @PostMapping("/v1/crm/sync")
    public ResponseEntity<Void> syncCampaigns(@RequestBody @Valid List<CrmIncomingCampaignDTO> campaigns){

        crmSyncService.processCampaigns(campaigns);

        return ResponseEntity.accepted().build();

    }

}
