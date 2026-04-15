package com.spx.mappers;

import com.spx.dto.CampaignReportDTO;
import com.spx.models.Attendee;
import com.spx.models.Campaign;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;

// MapStruct mapper responsible for converting Campaign entities into report-specific DTOs.

@Mapper(componentModel = "spring")
public interface CampaignReportMapper {

    /**
     * Maps a Campaign entity to a report section.
     *
     * @param campaign the source campaign entity
     * @return the mapped report section
     */
    @Mapping(target = "campaignDisplayName", expression = "java(buildCampaignDisplayName(campaign))")
    @Mapping(target = "attendeeCount", source = "attendees", qualifiedByName = "countAttendees")
    @Mapping(target = "attendeeRows", source = "attendees", qualifiedByName = "mapAttendeeRows")
    CampaignReportDTO toReportSection(Campaign campaign);

    /**
     * Builds a display-friendly campaign label for the report.
     *
     * @param campaign the source campaign entity
     * @return the campaign label shown in the report
     */
    @Named("buildCampaignDisplayName")
    default String buildCampaignDisplayName(Campaign campaign) {
        if (campaign == null || campaign.getCampaignId() == null) {
            return "";
        }

        if (campaign.getSubCampaignId() == null || campaign.getSubCampaignId().isBlank()) {
            return campaign.getCampaignId();
        }

        return campaign.getCampaignId() + " - " + campaign.getSubCampaignId();
    }

    /**
     * Counts the attendees of the selected campaign.
     *
     * @param attendees the attendee list
     * @return the number of attendees
     */
    @Named("countAttendees")
    default int countAttendees(List<Attendee> attendees) {
        return attendees == null ? 0 : attendees.size();
    }

    /**
     * Maps the attendee list to report rows.
     *
     * @param attendees the source attendee list
     * @return the mapped attendee rows
     */
    @Named("mapAttendeeRows")
    default List<CampaignReportDTO.AttendeeReportRow> mapAttendeeRows(List<Attendee> attendees) {
        if (attendees == null || attendees.isEmpty()) {
            return Collections.emptyList();
        }

        return attendees.stream()
                .map(this::toAttendeeReportRow)
                .toList();
    }

    /**
     * Maps one attendee entity to one report row.
     *
     * @param attendee the source attendee entity
     * @return the mapped attendee row
     */
    default CampaignReportDTO.AttendeeReportRow toAttendeeReportRow(Attendee attendee) {
        return new CampaignReportDTO.AttendeeReportRow(
                attendee.getFirstName(),
                attendee.getLastName(),
                attendee.getCn(),
                attendee.getBirthDate(),
                attendee.isCompanion()
        );
    }
}