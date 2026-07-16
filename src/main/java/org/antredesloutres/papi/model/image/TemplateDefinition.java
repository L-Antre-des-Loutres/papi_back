package org.antredesloutres.papi.model.image;

import java.util.List;

/**
 * A data-driven card template: a background image plus a list of positioned
 * elements. Definitions are plain JSON files loaded by TemplateService, so new
 * templates can be added without recompiling.
 *
 * <p>Being a record, {@link #hashCode()} is value-based over the whole
 * definition — it is used in the image state hash so that editing a template
 * file invalidates previously cached renders.</p>
 */
public record TemplateDefinition(
        String id,
        String name,
        String background,
        int referenceWidth,
        int referenceHeight,
        List<TemplateElement> elements
) {}
