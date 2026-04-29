package org.antredesloutres.papi.dto.response;

public record MoveResponse(
        Integer id,
        String symbol,
        TypeRefResponse type,
        int power,
        int accuracy,
        int pp
) {}
