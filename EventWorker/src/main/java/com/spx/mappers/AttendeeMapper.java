package com.spx.mappers;

import com.spx.dto.AttendeeDTO;
import com.spx.models.Attendee;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AttendeeMapper {

    Attendee toEntity(AttendeeDTO dto);
    List<Attendee> toEntityList(List<AttendeeDTO> dtoList);

}