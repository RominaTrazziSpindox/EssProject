package com.spx.services;

import com.spx.dto.CampaignAggregatedDataDTO;
import com.spx.dto.CampaignReportDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Collections;
import java.util.Objects;

/**
 * Service responsible for calculating campaign-level aggregated data
 * used by the Excel summary sheet and charts.
 */
@Service
public class CampaignAggregatedDataService {

    /**
     * Builds one aggregated data row starting from one campaign report DTO.
     *
     * @param campaignReport the source campaign report data
     * @return the calculated aggregate data for the selected campaign
     */
    public CampaignAggregatedDataDTO buildAggregateData(CampaignReportDTO campaignReport) {

        List<CampaignReportDTO.AttendeeReportRow> attendeeRows = campaignReport.attendeeRows();

        // Aggregated properties for each row (with methods filter and counts)
        int attendeeCount = attendeeRows.size();

        int mainAttendeeCount = (int) attendeeRows.stream()
                .filter(attendee -> !Boolean.TRUE.equals(attendee.companion()))
                .count();

        int companionCount = (int) attendeeRows.stream()
                .filter(attendee -> Boolean.TRUE.equals(attendee.companion()))
                .count();

        int missingCnCount = (int) attendeeRows.stream()
                .filter(attendee -> attendee.cn() == null || attendee.cn().isBlank())
                .count();

        int missingBirthDateCount = (int) attendeeRows.stream()
                .filter(attendee -> attendee.birthDate() == null)
                .count();

        int completeRecordCount = (int) attendeeRows.stream()
                .filter(attendee -> attendee.cn() != null && !attendee.cn().isBlank())
                .filter(attendee -> attendee.birthDate() != null)
                .count();

        int youngAttendeeCount = (int) attendeeRows.stream()
                .filter(attendee -> calculateAge(attendee.birthDate()) != null)
                .filter(attendee -> calculateAge(attendee.birthDate()) <= 29)
                .count();

        int adultAttendeeCount = (int) attendeeRows.stream()
                .filter(attendee -> calculateAge(attendee.birthDate()) != null)
                .filter(attendee -> {
                    int age = calculateAge(attendee.birthDate());
                    return age >= 30 && age <= 49;
                })
                .count();

        int seniorAttendeeCount = (int) attendeeRows.stream()
                .filter(attendee -> calculateAge(attendee.birthDate()) != null)
                .filter(attendee -> calculateAge(attendee.birthDate()) >= 50)
                .count();

        double companionRate = calculatePercentage(companionCount, attendeeCount);
        double mainAttendeeRate = calculatePercentage(mainAttendeeCount, attendeeCount);
        double dataCompletenessRate = calculatePercentage(completeRecordCount, attendeeCount);
        double averageAge = calculateAverageAge(attendeeRows);

        // Values returned
        return new CampaignAggregatedDataDTO(
                campaignReport.campaignDisplayName(), attendeeCount, mainAttendeeCount, companionCount, companionRate,
                mainAttendeeRate, campaignReport.subCampaignId() != null && !campaignReport.subCampaignId().isBlank(),
                missingCnCount, missingBirthDateCount, dataCompletenessRate, averageAge, youngAttendeeCount,
                adultAttendeeCount, seniorAttendeeCount
        );
    }

    /**
     * Builds aggregated data rows for all campaign report sections.
     *
     * @param campaignReports the source campaign report sections
     * @return the aggregated data rows used by the summary sheet
     */
    public List<CampaignAggregatedDataDTO> buildAggregateDataList(List<CampaignReportDTO> campaignReports) {
        if (campaignReports == null || campaignReports.isEmpty()) {
            return Collections.emptyList();
        }

        return campaignReports.stream()
                .map(this::buildAggregateData)
                .toList();
    }


    // --- HELPER FUNCTIONS ---


    /**
     * Helper function to calculate the age for the provided date of birth.
     *
     * @param birthDate the birthdate of the attendee
     * @return the age in years, or null when the date of birth is missing
     */
    private Integer calculateAge(LocalDate birthDate) {
        if (birthDate == null) {
            return null;
        }

        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * Helper function to calculate a percentage rounded to two decimal places.
     *
     * @param value the partial value
     * @param total the total value
     * @return the calculated percentage
     */
    private double calculatePercentage(int value, int total) {
        if (total == 0) {
            return 0.0;
        }

        double percentage = (double) value / total * 100;
        return Math.round(percentage * 100.0) / 100.0;
    }

    /**
     * Helper function to calculate the average age considering only attendees
     * with a non-null date of birth.
     *
     * @param attendeeRows the attendee rows of the selected campaign
     * @return the average age rounded to two decimal places
     */
    private double calculateAverageAge(List<CampaignReportDTO.AttendeeReportRow> attendeeRows) {
        List<Integer> ages = attendeeRows.stream()
                .map(attendee -> calculateAge(attendee.birthDate()))
                .filter(Objects::nonNull)
                .toList();

        if (ages.isEmpty()) {
            return 0.0;
        }

        double average = ages.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        return Math.round(average * 100.0) / 100.0;
    }
}