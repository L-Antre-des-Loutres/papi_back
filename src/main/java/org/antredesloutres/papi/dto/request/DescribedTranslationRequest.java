package org.antredesloutres.papi.dto.request;

import jakarta.validation.constraints.NotBlank;

/** Generic body for translations with a name and a description (Ability, Move). */
public record DescribedTranslationRequest(
        @NotBlank String name,
        String description
) {}
