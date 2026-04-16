package com.spx.dto;

/**
 * DTO representing one aggregated row inside the Excel summary sheet.
 * This model contains campaign-level calculated values used for reporting
 * and chart generation.
 */
public record CampaignAggregatedDataDTO (
        String campaignDisplayName,
        int attendeeCount,
        int mainAttendeeCount,
        int companionCount,
        double companionRate,
        double mainAttendeeRate,
        boolean hasSubCampaign,
        int missingCnCount,
        int missingBirthDateCount,
        double dataCompletenessRate,
        double averageAge,
        int youngAttendeeCount,
        int adultAttendeeCount,
        int seniorAttendeeCount
) {
}