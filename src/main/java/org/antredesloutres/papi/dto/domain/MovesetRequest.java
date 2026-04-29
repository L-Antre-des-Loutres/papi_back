package org.antredesloutres.papi.dto.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.antredesloutres.papi.model.enumerated.MoveLearnMethod;

public record MovesetRequest(
        @NotNull Integer moveId,
        @NotNull MoveLearnMethod learnMethod,
        @Min(1) Integer learnLevel
) {}
