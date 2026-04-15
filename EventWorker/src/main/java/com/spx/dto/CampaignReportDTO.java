package com.spx.dto;

import java.time.LocalDate;
import java.util.List;

// DTO used exclusively by the reporting flow with record data type
public record CampaignReportDTO(
        String campaignId,
        String subCampaignId,
        String campaignDisplayName,
        int attendeeCount,

        List<AttendeeReportRow> attendeeRows) {

            // DTO representing one attendee row inside a campaign report section
            public record AttendeeReportRow(
                    String firstName,
                    String lastName,
                    String cn,
                    LocalDate birthDate,
                    Boolean companion) {}

        }