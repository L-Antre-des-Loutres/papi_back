package org.antredesloutres.papi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ColorRequest(
        @NotBlank
        @Pattern(regexp = "^#?[0-9a-fA-F]{6}$", message = "color must be a 6-digit hex code (with or without #)")
        String color
) {}
