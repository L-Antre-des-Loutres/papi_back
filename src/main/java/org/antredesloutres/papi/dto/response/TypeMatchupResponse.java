package org.antredesloutres.papi.dto.response;

import org.antredesloutres.papi.model.enumerated.Effectiveness;

public record TypeMatchupResponse(
        Long id,
        TypeRefResponse attackingType,
        TypeRefResponse defendingType,
        Effectiveness effectiveness
) {}
