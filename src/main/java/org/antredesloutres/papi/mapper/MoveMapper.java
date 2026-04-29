package org.antredesloutres.papi.mapper;

import org.antredesloutres.papi.dto.response.MoveResponse;
import org.antredesloutres.papi.dto.response.MoveTranslationResponse;
import org.antredesloutres.papi.model.domain.Move;
import org.antredesloutres.papi.model.translation.MoveTranslation;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = TypeMapper.class)
public interface MoveMapper {

    MoveResponse toResponse(Move move);

    List<MoveResponse> toResponseList(List<Move> moves);

    MoveTranslationResponse toTranslationResponse(MoveTranslation translation);

    List<MoveTranslationResponse> toTranslationResponseList(List<MoveTranslation> translations);
}
