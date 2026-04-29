package org.antredesloutres.papi.dto.request;

/**
 * Body for endpoints that accept a single type id (nullable to detach).
 * Example: PATCH /api/moves/{id}/type with { "typeId": 3 }
 */
public record TypeRefRequest(Integer typeId) {}
