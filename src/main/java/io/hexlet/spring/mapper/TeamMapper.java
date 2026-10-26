package io.hexlet.spring.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import io.hexlet.spring.dto.TeamCreateDTO;
import io.hexlet.spring.dto.TeamDTO;
import io.hexlet.spring.dto.TeamUpdateDTO;
import io.hexlet.spring.model.Team;

@Mapper(
        uses = { JsonNullableMapper.class, ReferenceMapper.class },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class TeamMapper {

    public abstract Team map(TeamCreateDTO dto);

    public abstract TeamDTO map(Team model);

    public abstract void update(TeamUpdateDTO dto, @MappingTarget Team model);
}
