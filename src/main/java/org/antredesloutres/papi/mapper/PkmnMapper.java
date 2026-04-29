package org.antredesloutres.papi.mapper;

import org.antredesloutres.papi.dto.response.PkmnResponse;
import org.antredesloutres.papi.dto.response.PkmnSummaryResponse;
import org.antredesloutres.papi.dto.response.PkmnTranslationResponse;
import org.antredesloutres.papi.model.domain.Pkmn;
import org.antredesloutres.papi.model.translation.PkmnTranslation;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {TypeMapper.class, AbilityMapper.class})
public interface PkmnMapper {

    PkmnResponse toResponse(Pkmn pkmn);

    PkmnSummaryResponse toSummary(Pkmn pkmn);

    List<PkmnResponse> toResponseList(List<Pkmn> pkmns);

    List<PkmnSummaryResponse> toSummaryList(List<Pkmn> pkmns);

    PkmnTranslationResponse toTranslationResponse(PkmnTranslation translation);

    List<PkmnTranslationResponse> toTranslationResponseList(List<PkmnTranslation> translations);
}
