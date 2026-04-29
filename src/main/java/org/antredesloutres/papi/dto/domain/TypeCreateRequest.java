package org.antredesloutres.papi.dto.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TypeCreateRequest(
        @NotBlank @Size(min = 1, max = 100) String symbol,
        @Pattern(regexp = "^#?[0-9a-fA-F]{6}$", message = "color must be a 6-digit hex code (with or without #)")
        String color,
        String nameEN
) {}
