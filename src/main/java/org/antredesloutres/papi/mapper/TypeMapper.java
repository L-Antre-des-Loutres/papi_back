package org.antredesloutres.papi.mapper;

import org.antredesloutres.papi.dto.response.TypeRefResponse;
import org.antredesloutres.papi.dto.response.TypeResponse;
import org.antredesloutres.papi.dto.response.TypeTranslationResponse;
import org.antredesloutres.papi.model.domain.Type;
import org.antredesloutres.papi.model.translation.TypeTranslation;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TypeMapper {

    TypeResponse toResponse(Type type);

    TypeRefResponse toRef(Type type);

    List<TypeResponse> toResponseList(List<Type> types);

    TypeTranslationResponse toTranslationResponse(TypeTranslation translation);

    List<TypeTranslationResponse> toTranslationResponseList(List<TypeTranslation> translations);
}
