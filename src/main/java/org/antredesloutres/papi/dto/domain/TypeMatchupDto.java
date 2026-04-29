package org.antredesloutres.papi.dto.domain;

import jakarta.validation.constraints.NotNull;
import org.antredesloutres.papi.model.enumerated.Effectiveness;

public record TypeMatchupDto(
        @NotNull Integer attackingTypeId,
        @NotNull Integer defendingTypeId,
        @NotNull Effectiveness effectiveness
) {}
