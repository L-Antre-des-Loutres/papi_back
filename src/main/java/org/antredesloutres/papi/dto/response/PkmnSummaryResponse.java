package org.antredesloutres.papi.dto.response;

/** Lightweight Pkmn shape for list endpoints. */
public record PkmnSummaryResponse(
        Integer id,
        String symbol,
        Integer nationalDexNumber,
        TypeRefResponse primaryType,
        TypeRefResponse secondaryType
) {}
