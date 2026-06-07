package org.antredesloutres.papi.dto.response;

import java.time.Instant;
import java.util.Set;

public record PkmnImageResponse(
        Long id,
        String url,
        String name,
        Set<String> tags,
        boolean main,
        Instant addedAt
) {}
