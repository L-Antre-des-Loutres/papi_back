package org.antredesloutres.papi.controller;

import org.antredesloutres.papi.config.SecurityConfig;
import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.antredesloutres.papi.exception.GlobalExceptionHandler;
import org.antredesloutres.papi.model.domain.Pkmn;
import org.antredesloutres.papi.model.image.TemplateDefinition;
import org.antredesloutres.papi.model.image.TemplateElement;
import org.antredesloutres.papi.model.image.TemplateElementType;
import org.antredesloutres.papi.repository.image.ImageMetadataRepository;
import org.antredesloutres.papi.security.JwtAuthFilter;
import org.antredesloutres.papi.service.domain.PkmnImageService;
import org.antredesloutres.papi.service.domain.PkmnService;
import org.antredesloutres.papi.service.image.ImageGeneratorService;
import org.antredesloutres.papi.service.image.ImageService;
import org.antredesloutres.papi.service.image.TemplateService;
import org.antredesloutres.papi.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ImageController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, JwtAuthFilter.class}))
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageService imageService;

    @MockBean
    private ImageGeneratorService imageGeneratorService;

    @MockBean
    private PkmnService pkmnService;

    @MockBean
    private PkmnImageService pkmnImageService;

    @MockBean
    private TemplateService templateService;

    @MockBean
    private ImageMetadataRepository imageMetadataRepository;

    private static TemplateDefinition summaryTemplate() {
        return new TemplateDefinition("summary", "Pokémon Summary Card", "template/pokemon_summary.png", 1920, 1080,
                List.of(new TemplateElement(TemplateElementType.NAME, 770, 100, 660, 115)));
    }

    @Test
    void getImage_ShouldReturnImage_WhenFileExists() throws Exception {
        // Arrange
        String filename = "test.png";
        byte[] content = "fake image content".getBytes();
        Resource resource = new ByteArrayResource(content);
        when(imageService.loadImage(filename)).thenReturn(resource);

        // Act & Assert
        mockMvc.perform(get("/api/images/" + filename))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG_VALUE))
                .andExpect(content().bytes(content));
    }

    @Test
    void getImage_ShouldReturnNotFound_WhenFileDoesNotExist() throws Exception {
        // Arrange
        String filename = "missing.png";
        when(imageService.loadImage(filename)).thenThrow(new EntityNotFoundException("Image", filename));

        // Act & Assert
        mockMvc.perform(get("/api/images/" + filename))
                .andExpect(status().isNotFound());
    }

    @Test
    void listTemplates_ShouldReturnTemplates() throws Exception {
        // Arrange
        when(templateService.listTemplates()).thenReturn(List.of(summaryTemplate()));

        // Act & Assert
        mockMvc.perform(get("/api/images/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("summary"))
                .andExpect(jsonPath("$[0].name").value("Pokémon Summary Card"));
    }

    @Test
    void renderPokemonImage_ShouldStreamPng() throws Exception {
        // Arrange
        Pkmn pkmn = TestFixtures.pkmn(1, "bulbasaur");
        when(pkmnService.getPkmnById(1)).thenReturn(pkmn);
        when(pkmnImageService.resolveSpriteUrl(eq(pkmn), isNull())).thenReturn("http://example.com/sprite.png");
        when(templateService.getByIdOrDefault(isNull())).thenReturn(summaryTemplate());
        when(imageGeneratorService.generatePkmnInfoImage(eq(pkmn), any(), anyString(), any(TemplateDefinition.class)))
                .thenReturn(new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB));

        // Act & Assert
        mockMvc.perform(get("/api/images/render/pokemon/1?language=FR"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG_VALUE))
                .andExpect(header().string("Cache-Control", "no-store"));
    }

    @Test
    void renderPokemonImage_ShouldReturnNotFound_WhenTemplateUnknown() throws Exception {
        // Arrange
        Pkmn pkmn = TestFixtures.pkmn(1, "bulbasaur");
        when(pkmnService.getPkmnById(1)).thenReturn(pkmn);
        when(pkmnImageService.resolveSpriteUrl(eq(pkmn), isNull())).thenReturn("http://example.com/sprite.png");
        when(templateService.getByIdOrDefault("nope")).thenThrow(new EntityNotFoundException("Template", "nope"));

        // Act & Assert
        mockMvc.perform(get("/api/images/render/pokemon/1?template=nope"))
                .andExpect(status().isNotFound());
    }

    @Test
    void generatePokemonImage_ShouldPersistWithTemplateInFilename() throws Exception {
        // Arrange
        Pkmn pkmn = TestFixtures.pkmn(1, "bulbasaur");
        byte[] content = "fake image content".getBytes();
        when(pkmnService.getPkmnById(1)).thenReturn(pkmn);
        when(pkmnImageService.resolveSpriteUrl(eq(pkmn), isNull())).thenReturn("http://example.com/sprite.png");
        when(templateService.getByIdOrDefault(isNull())).thenReturn(summaryTemplate());
        when(imageGeneratorService.calculateStateHash(eq(pkmn), any(), anyString(), any(TemplateDefinition.class)))
                .thenReturn("hash");
        when(imageMetadataRepository.findByPkmnIdAndLanguageAndTemplate(eq(1), any(), eq("summary")))
                .thenReturn(Optional.empty());
        when(imageGeneratorService.generatePkmnInfoImage(eq(pkmn), any(), anyString(), any(TemplateDefinition.class)))
                .thenReturn(new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB));
        when(imageService.loadImage("pkmn-1-fr-summary.png")).thenReturn(new ByteArrayResource(content));

        // Act & Assert
        mockMvc.perform(post("/api/images/generate/pokemon/1?language=FR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filename").value("pkmn-1-fr-summary.png"));
    }
}
