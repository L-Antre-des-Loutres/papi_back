package org.antredesloutres.papi.dto.response;

public record ImageResponse(
        String filename,
        String contentType,
        long size,
        String url
) {}
