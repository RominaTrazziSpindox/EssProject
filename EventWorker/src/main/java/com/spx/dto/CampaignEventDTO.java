package com.spx.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
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

    @NotEmpty
    @Size(min = 1)
    @Valid
    private List<AttendeeDTO> attendees;

}