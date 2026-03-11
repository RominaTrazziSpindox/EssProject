package com.spx.mappers;

import com.spx.dto.AttendeeDTO;
import com.spx.models.Attendee;
import org.mapstruct.Mapper;

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

    /*
     * Converts a single AttendeeDTO into an Attendee entity.
     */
    Attendee toEntity(AttendeeDTO dto);


    List<Attendee> toEntityList(List<AttendeeDTO> dtoList);

}