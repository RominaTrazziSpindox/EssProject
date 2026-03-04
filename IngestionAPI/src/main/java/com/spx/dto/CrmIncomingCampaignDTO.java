package com.spx.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


// It represents the IncomingCampaign from external CRM software
@Data
@NoArgsConstructor
public class CrmIncomingCampaignDTO {

    @NotBlank
    private String campaignId;

    private String subCampaignId;

    @NotEmpty
    @Valid
    private List<CrmIncomingAttendeeDTO> attendees;

}



