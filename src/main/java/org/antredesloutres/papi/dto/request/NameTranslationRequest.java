package org.antredesloutres.papi.dto.request;

import jakarta.validation.constraints.NotBlank;

/** Body for endpoints saving a translation that has only a name (e.g. Type). */
public record NameTranslationRequest(@NotBlank String name) {}
