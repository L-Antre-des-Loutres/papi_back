package org.antredesloutres.papi.dto.response;

/** Lightweight Type reference embedded in other responses (e.g. Move, Pkmn). */
public record TypeRefResponse(Integer id, String symbol, String color) {}
