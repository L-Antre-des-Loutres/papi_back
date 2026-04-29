package org.antredesloutres.papi.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record TagsRequest(
        @NotNull Set<String> tags
) {}
