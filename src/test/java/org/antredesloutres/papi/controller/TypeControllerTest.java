package org.antredesloutres.papi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.antredesloutres.papi.config.SecurityConfig;
import org.antredesloutres.papi.dto.response.TypeResponse;
import org.antredesloutres.papi.dto.response.TypeTranslationResponse;
import org.antredesloutres.papi.exception.GlobalExceptionHandler;
import org.antredesloutres.papi.mapper.TypeMapper;
import org.antredesloutres.papi.model.domain.Type;
import org.antredesloutres.papi.model.enumerated.Language;
import org.antredesloutres.papi.model.translation.TypeTranslation;
import org.antredesloutres.papi.security.JwtAuthFilter;
import org.antredesloutres.papi.service.domain.TypeService;
import org.antredesloutres.papi.service.translation.TypeTranslationService;
import org.antredesloutres.papi.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TypeController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, JwtAuthFilter.class}))
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class TypeControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    TypeService typeService;
    @MockBean
    TypeTranslationService typeTranslationService;
    @MockBean
    TypeMapper typeMapper;

    @Test
    void getAll_returnsPage() throws Exception {
        // arrange
        Type type = TestFixtures.type(1, "fire");
        when(typeService.getAllTypes(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(type)));
        when(typeMapper.toResponse(type)).thenReturn(new TypeResponse(1, "fire", "#ff0000", Set.of()));

        // act + assert
        mockMvc.perform(get("/api/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].symbol").value("fire"));
    }

    @Test
    void getById_returnsType() throws Exception {
        // arrange
        Type type = TestFixtures.type(1, "fire");
        when(typeService.getTypeById(1)).thenReturn(type);
        when(typeMapper.toResponse(type)).thenReturn(new TypeResponse(1, "fire", "#ff0000", Set.of()));

        // act + assert
        mockMvc.perform(get("/api/types/1"))
                .andExpect(status().isOk());
    }

    @Test
    void create_returns201() throws Exception {
        // arrange
        Type created = TestFixtures.type(2, "fairy");
        when(typeService.createDefaultType(any(), any(), any())).thenReturn(created);
        when(typeMapper.toResponse(created)).thenReturn(new TypeResponse(2, "fairy", "#ee99ac", Set.of()));

        // act + assert
        mockMvc.perform(post("/api/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "symbol", "fairy",
                                "color", "#ee99ac",
                                "nameEN", "Fairy"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.symbol").value("fairy"));
    }

    @Test
    void create_returns400WhenSymbolBlank() throws Exception {
        // arrange + act + assert
        mockMvc.perform(post("/api/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "symbol", "",
                                "color", "#ee99ac"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_returns400WhenColorMalformed() throws Exception {
        // arrange + act + assert
        mockMvc.perform(post("/api/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "symbol", "fairy",
                                "color", "not-a-color"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createDefault_returns201() throws Exception {
        // arrange
        Type created = TestFixtures.type(3, "new-type");
        when(typeService.createDefaultType(any(), any(), any())).thenReturn(created);
        when(typeMapper.toResponse(created)).thenReturn(new TypeResponse(3, "new-type", "#abcdef", Set.of()));

        // act + assert
        mockMvc.perform(post("/api/types/default"))
                .andExpect(status().isCreated());
    }

    @Test
    void addTag_returnsUpdatedType() throws Exception {
        // arrange
        Type updated = TestFixtures.type(1, "fire");
        updated.setTags(Set.of("custom"));
        when(typeService.addTag(1, "custom")).thenReturn(updated);
        when(typeMapper.toResponse(updated)).thenReturn(new TypeResponse(1, "fire", "#ff0000", Set.of("custom")));

        // act + assert
        mockMvc.perform(post("/api/types/1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("tag", "custom"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags[0]").value("custom"));
    }

    @Test
    void updateColor_acceptsHexWithoutHash() throws Exception {
        // arrange
        Type updated = TestFixtures.type(1, "fire");
        when(typeService.updateColor(eq(1), any())).thenReturn(updated);
        when(typeMapper.toResponse(updated)).thenReturn(new TypeResponse(1, "fire", "#abc123", Set.of()));

        // act + assert
        mockMvc.perform(patch("/api/types/1/color")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("color", "abc123"))))
                .andExpect(status().isOk());
    }

    @Test
    void removeTag_returnsType() throws Exception {
        // arrange
        Type updated = TestFixtures.type(1, "fire");
        when(typeService.removeTag(1, "custom")).thenReturn(updated);
        when(typeMapper.toResponse(updated)).thenReturn(new TypeResponse(1, "fire", "#ff0000", Set.of()));

        // act + assert
        mockMvc.perform(delete("/api/types/1/tags/custom"))
                .andExpect(status().isOk());
    }

    @Test
    void delete_returns204() throws Exception {
        // arrange + act + assert
        mockMvc.perform(delete("/api/types/1"))
                .andExpect(status().isNoContent());
        verify(typeService).deleteType(1);
    }

    @Test
    void getTranslations_returnsList() throws Exception {
        // arrange
        TypeTranslation tr = new TypeTranslation(Language.FR, "Feu");
        when(typeTranslationService.getTypeTranslations(1)).thenReturn(List.of(tr));
        when(typeMapper.toTranslationResponseList(List.of(tr)))
                .thenReturn(List.of(new TypeTranslationResponse(1, Language.FR, "Feu")));

        // act + assert
        mockMvc.perform(get("/api/types/1/translations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Feu"));
    }

    @Test
    void saveTranslation_returnsTranslation() throws Exception {
        // arrange
        TypeTranslation saved = new TypeTranslation(Language.FR, "Feu");
        when(typeTranslationService.saveTypeTranslation(eq(1), any(), any())).thenReturn(saved);
        when(typeMapper.toTranslationResponse(saved))
                .thenReturn(new TypeTranslationResponse(1, Language.FR, "Feu"));

        // act + assert
        mockMvc.perform(put("/api/types/1/translations/FR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "Feu"))))
                .andExpect(status().isOk());
    }

}
