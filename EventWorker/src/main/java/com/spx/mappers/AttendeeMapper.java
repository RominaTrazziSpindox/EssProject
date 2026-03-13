package com.spx.mappers;

import com.spx.dto.AttendeeDTO;
import com.spx.models.Attendee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/*
 * MapStruct mapper responsible for converting AttendeeDTO objects
 * into Attendee entities.
 *
 * This mapper is used by CampaignMapper when mapping the list of
 * attendees contained in CampaignEventDTO.
 */
@Mapper(componentModel = "spring")
public interface AttendeeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "campaign", ignore = true)
    @Mapping(target = "companion", source = "isCompanion")

    // Converts a single AttendeeDTO into an Attendee entity.
    Attendee toEntity(AttendeeDTO dto);

    // Converts a DTO List (= CRM payload) into a for cycle of single DTOs
    List<Attendee> toEntityList(List<AttendeeDTO> dtoList);

}