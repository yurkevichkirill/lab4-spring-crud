package io.hexlet.spring.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import io.hexlet.spring.dto.PlayerCreateDTO;
import io.hexlet.spring.dto.PlayerDTO;
import io.hexlet.spring.dto.PlayerUpdateDTO;
import io.hexlet.spring.model.Player;

@Mapper(
        uses = { JsonNullableMapper.class, ReferenceMapper.class },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class PlayerMapper {

    @Mapping(target = "team", source = "teamId")
    public abstract Player map(PlayerCreateDTO dto);

    @Mapping(target = "teamId", source = "team.id")
    @Mapping(target = "teamName", source = "team.name")
    public abstract PlayerDTO map(Player model);

    @Mapping(target = "team", source = "teamId")
    public abstract void update(PlayerUpdateDTO dto, @MappingTarget Player model);
}
