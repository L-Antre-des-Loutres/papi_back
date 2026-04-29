package org.antredesloutres.papi.dto.response;

import java.util.Set;

public record TypeResponse(
        Integer id,
        String symbol,
        String color,
        Set<String> tags
) {}
