package org.antredesloutres.papi.model.image;

/**
 * One block of a template layout. Coordinates are expressed in the template's
 * reference space (referenceWidth x referenceHeight) and scaled at render time.
 */
public record TemplateElement(
        TemplateElementType type,
        int x,
        int y,
        int w,
        int h
) {}
