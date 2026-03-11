package com.spx.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
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
    @Size(min = 1)
    @Valid
    private List<CrmIncomingAttendeeDTO> attendees;

}



