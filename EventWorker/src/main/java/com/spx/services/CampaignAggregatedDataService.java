package com.spx.services;

import com.spx.dto.CampaignAggregatedDataDTO;
import com.spx.dto.CampaignReportDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

// Service responsible for calculating campaign aggregated data used by the Excel summary sheet and charts.

@Service
public class CampaignAggregatedDataService {

    /**
     * Builds one aggregated data row starting from one CampaignReportDTO
     * which contains both type of data (from Campaigns and from Attendees).
     *
     * @param campaignReport the source campaign report data
     * @return the calculated aggregate data for the selected campaign in
     * a new DTO called CampaignAggregatedDataDTO.
     */
    public CampaignAggregatedDataDTO buildAggregatedData(CampaignReportDTO campaignReport) {

        // Retrieve attendee rows from the DTO or use an empty list if the source list is null
        List<CampaignReportDTO.AttendeeReportRow> attendeeRows = campaignReport.attendeeRows() == null ? Collections.emptyList() : campaignReport.attendeeRows();

        // Calculate aggregated properties for each row of attendees (with filter and counts)
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

        // Build the "ages list"
        List<Integer> ages = attendeeRows
                .stream()
                .map(CampaignReportDTO.AttendeeReportRow::birthDate)
                .map(this::calculateAge)
                .filter(Objects::nonNull)
                .toList();

        int youngAttendeeCount = (int) ages.stream()
                .filter(age -> age <= 29)
                .count();

        int adultAttendeeCount = (int) ages.stream()
                .filter(age -> age >= 30 && age <= 49)
                .count();

        int seniorAttendeeCount = (int) ages.stream()
                .filter(age -> age >= 50)
                .count();

        // Percentage values
        double companionRate = calculatePercentage(companionCount, attendeeCount);
        double mainAttendeeRate = calculatePercentage(mainAttendeeCount, attendeeCount);
        double dataCompletenessRate = calculatePercentage(completeRecordCount, attendeeCount);
        double averageAge = calculateAverageAge(ages);

        // Values returned
        return new CampaignAggregatedDataDTO(
                campaignReport.campaignDisplayName(),
                attendeeCount,
                mainAttendeeCount,
                companionCount,
                companionRate,
                mainAttendeeRate,
                campaignReport.subCampaignId() != null && !campaignReport.subCampaignId().isBlank(),
                missingCnCount,
                missingBirthDateCount,
                dataCompletenessRate,
                averageAge,
                youngAttendeeCount,
                adultAttendeeCount,
                seniorAttendeeCount
        );
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
     * @param ages the calculated ages of the selected campaign attendees
     * @return the average age rounded to two decimal places
     */
    private double calculateAverageAge(List<Integer> ages) {
        if (ages == null || ages.isEmpty()) {
            return 0.0;
        }

        double average = ages.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        return Math.round(average * 100.0) / 100.0;
    }
}