package org.antredesloutres.papi.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Generic single-integer body for PATCH endpoints (power, accuracy, pp, nationalDexNumber, etc.).
 */
public record IntValueRequest(@NotNull Integer value) {}
