package org.antredesloutres.papi.service.image;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.antredesloutres.papi.model.image.TemplateDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemplateServiceTest {

    @TempDir
    Path externalDir;

    private TemplateService templateService;

    @BeforeEach
    void setUp() {
        templateService = new TemplateService(new ObjectMapper(), externalDir.toString(), "summary");
    }

    private void writeExternal(String filename, String json) throws IOException {
        Files.writeString(externalDir.resolve(filename), json);
    }

    @Test
    void loadTemplates_LoadsBuiltInSummary() {
        // Act
        templateService.loadTemplates();

        // Assert
        TemplateDefinition summary = templateService.getById("summary");
        assertEquals("summary", summary.id());
        assertEquals(1920, summary.referenceWidth());
        assertEquals(7, summary.elements().size());
    }

    @Test
    void getById_ShouldThrow_WhenUnknown() {
        // Arrange
        templateService.loadTemplates();

        // Act + Assert
        assertThrows(EntityNotFoundException.class, () -> templateService.getById("does-not-exist"));
    }

    @Test
    void getByIdOrDefault_ShouldReturnDefault_WhenIdIsNullOrBlank() {
        // Arrange
        templateService.loadTemplates();

        // Act + Assert
        assertEquals("summary", templateService.getByIdOrDefault(null).id());
        assertEquals("summary", templateService.getByIdOrDefault("  ").id());
    }

    @Test
    void loadTemplates_ExternalFile_AddsAndOverrides() throws IOException {
        // Arrange: one new template, one overriding the built-in "summary"
        writeExternal("custom.json", """
                { "id": "custom", "name": "Custom", "background": "template/custom.png",
                  "referenceWidth": 1000, "referenceHeight": 500,
                  "elements": [ { "type": "NAME", "x": 0, "y": 0, "w": 100, "h": 50 } ] }
                """);
        writeExternal("summary-override.json", """
                { "id": "summary", "name": "Overridden Summary", "background": "template/pokemon_summary.png",
                  "referenceWidth": 1920, "referenceHeight": 1080,
                  "elements": [] }
                """);

        // Act
        templateService.loadTemplates();

        // Assert
        List<TemplateDefinition> all = templateService.listTemplates();
        assertTrue(all.stream().anyMatch(t -> t.id().equals("custom")));
        assertEquals("Overridden Summary", templateService.getById("summary").name());
    }

    @Test
    void loadTemplates_ShouldSkipInvalidDefinitions() throws IOException {
        // Arrange: filename-unsafe id (would allow path traversal in generated filenames) + malformed JSON
        writeExternal("evil.json", """
                { "id": "../evil", "name": "Evil", "background": "x.png",
                  "referenceWidth": 100, "referenceHeight": 100, "elements": [] }
                """);
        writeExternal("broken.json", "{ not json");

        // Act
        templateService.loadTemplates();

        // Assert: only the built-in remains
        assertEquals(1, templateService.listTemplates().size());
        assertThrows(EntityNotFoundException.class, () -> templateService.getById("../evil"));
    }
}
