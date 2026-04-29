package org.antredesloutres.papi.mapper;

import org.antredesloutres.papi.dto.response.TypeMatchupResponse;
import org.antredesloutres.papi.model.domain.TypeMatchup;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = TypeMapper.class)
public interface TypeMatchupMapper {

    TypeMatchupResponse toResponse(TypeMatchup matchup);

    List<TypeMatchupResponse> toResponseList(List<TypeMatchup> matchups);
}
