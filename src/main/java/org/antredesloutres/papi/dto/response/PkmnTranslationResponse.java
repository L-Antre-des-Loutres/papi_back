package org.antredesloutres.papi.dto.response;

import org.antredesloutres.papi.model.enumerated.Language;

public record PkmnTranslationResponse(
        Integer id,
        Language language,
        String pkmnName,
        String formName,
        String description
) {}
