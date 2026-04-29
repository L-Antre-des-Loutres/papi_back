package org.antredesloutres.papi.dto.response;

import org.antredesloutres.papi.model.enumerated.MoveLearnMethod;

public record MovesetResponse(
        Integer id,
        Integer pkmnId,
        MoveResponse move,
        MoveLearnMethod learnMethod,
        Integer learnLevel
) {}
