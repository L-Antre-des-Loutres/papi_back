package org.antredesloutres.papi.dto.domain;

import jakarta.validation.constraints.NotBlank;

public record PkmnTranslationRequest(
        @NotBlank String pkmnName,
        String formName,
        String description
) {}
