package org.antredesloutres.papi.dto.response;

import java.util.List;

public record AbilityResponse(
        Integer id,
        String symbol,
        List<AbilityTranslationResponse> lang
) {}
