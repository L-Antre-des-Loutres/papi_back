package org.antredesloutres.papi.dto.response;

import org.antredesloutres.papi.model.enumerated.Language;

public record TypeTranslationResponse(
        Integer id,
        Language language,
        String name
) {}
