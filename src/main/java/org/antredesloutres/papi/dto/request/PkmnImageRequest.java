package org.antredesloutres.papi.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record PkmnImageRequest(
        @NotBlank String url,
        String name,
        Set<String> tags,
        boolean main
) {}
