package org.antredesloutres.papi.dto.response;

import org.antredesloutres.papi.model.enumerated.Language;

public record AbilityTranslationResponse(
        Integer id,
        Language language,
        String name,
        String description
) {}
