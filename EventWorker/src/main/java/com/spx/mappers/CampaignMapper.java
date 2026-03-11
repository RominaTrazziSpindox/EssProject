package com.spx.mappers;

import com.spx.dto.CampaignEventDTO;
import com.spx.models.Campaign;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CampaignMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "attendees", ignore = true)
    Campaign toEntity(CampaignEventDTO dto);

}


