package org.antredesloutres.papi.dto.response;

import org.antredesloutres.papi.model.enumerated.Language;

public record MoveTranslationResponse(
        Integer id,
        Language language,
        String name,
        String description
) {}
