package org.antredesloutres.papi.mapper;

import org.antredesloutres.papi.dto.response.AbilityRefResponse;
import org.antredesloutres.papi.dto.response.AbilityResponse;
import org.antredesloutres.papi.dto.response.AbilityTranslationResponse;
import org.antredesloutres.papi.model.domain.Ability;
import org.antredesloutres.papi.model.translation.AbilityTranslation;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AbilityMapper {

    AbilityResponse toResponse(Ability ability);

    AbilityRefResponse toRef(Ability ability);

    List<AbilityResponse> toResponseList(List<Ability> abilities);

    AbilityTranslationResponse toTranslationResponse(AbilityTranslation translation);

    List<AbilityTranslationResponse> toTranslationResponseList(List<AbilityTranslation> translations);
}
