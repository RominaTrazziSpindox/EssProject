package com.spx.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignEventDTO {

    @NotBlank
    private String campaignId;

    private String subCampaignId;

    @NotNull
    @Valid
    private List<AttendeeDTO> attendees;

}