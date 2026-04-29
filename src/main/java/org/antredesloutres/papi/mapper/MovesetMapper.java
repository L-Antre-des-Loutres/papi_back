package org.antredesloutres.papi.mapper;

import org.antredesloutres.papi.dto.response.MovesetResponse;
import org.antredesloutres.papi.model.domain.Moveset;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = MoveMapper.class)
public interface MovesetMapper {

    @Mapping(source = "pkmn.id", target = "pkmnId")
    MovesetResponse toResponse(Moveset moveset);

    List<MovesetResponse> toResponseList(List<Moveset> movesets);
}
