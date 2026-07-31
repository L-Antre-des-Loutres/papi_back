package org.antredesloutres.papi.dto.response;

import java.util.List;

public record MoveResponse(
        Integer id,
        String symbol,
        TypeRefResponse type,
        int power,
        int accuracy,
        int pp,
        List<MoveTranslationResponse> lang
) {}
