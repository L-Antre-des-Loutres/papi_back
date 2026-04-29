package org.antredesloutres.papi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TagRequest(
        @NotBlank @Size(min = 1, max = 100) String tag
) {}
