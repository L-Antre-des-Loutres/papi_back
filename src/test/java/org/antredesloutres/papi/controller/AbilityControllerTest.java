package org.antredesloutres.papi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.antredesloutres.papi.config.SecurityConfig;
import org.antredesloutres.papi.dto.response.AbilityResponse;
import org.antredesloutres.papi.dto.response.AbilityTranslationResponse;
import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.antredesloutres.papi.exception.GlobalExceptionHandler;
import org.antredesloutres.papi.mapper.AbilityMapper;
import org.antredesloutres.papi.model.domain.Ability;
import org.antredesloutres.papi.model.enumerated.Language;
import org.antredesloutres.papi.model.translation.AbilityTranslation;
import org.antredesloutres.papi.security.JwtAuthFilter;
import org.antredesloutres.papi.service.domain.AbilityService;
import org.antredesloutres.papi.service.translation.AbilityTranslationService;
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

@WebMvcTest(controllers = AbilityController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, JwtAuthFilter.class}))
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AbilityControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    AbilityService abilityService;
    @MockBean
    AbilityTranslationService abilityTranslationService;
    @MockBean
    AbilityMapper abilityMapper;

    @Test
    void getAll_returnsPagedResponses() throws Exception {
        // arrange
        Ability ability = TestFixtures.ability(1, "static");
        when(abilityService.getAllAbilities(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ability)));
        when(abilityMapper.toResponse(ability)).thenReturn(new AbilityResponse(1, "static"));

        // act + assert
        mockMvc.perform(get("/api/abilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].symbol").value("static"));
    }

    @Test
    void getCount_returnsInt() throws Exception {
        // arrange
        when(abilityService.getTotalCount()).thenReturn(7);

        // act + assert
        mockMvc.perform(get("/api/abilities/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(7));
    }

    @Test
    void getById_returnsAbility() throws Exception {
        // arrange
        Ability ability = TestFixtures.ability(1, "static");
        when(abilityService.getAbilityById(1)).thenReturn(ability);
        when(abilityMapper.toResponse(ability)).thenReturn(new AbilityResponse(1, "static"));

        // act + assert
        mockMvc.perform(get("/api/abilities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("static"));
    }

    @Test
    void getById_returns404WhenMissing() throws Exception {
        // arrange
        when(abilityService.getAbilityById(99))
                .thenThrow(new EntityNotFoundException("Ability", 99));

        // act + assert
        mockMvc.perform(get("/api/abilities/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void createDefault_returns201() throws Exception {
        // arrange
        Ability ability = TestFixtures.ability(5, "new-ability-abc");
        when(abilityService.createDefaultAbility(any())).thenReturn(ability);
        when(abilityMapper.toResponse(ability)).thenReturn(new AbilityResponse(5, "new-ability-abc"));

        // act + assert
        mockMvc.perform(post("/api/abilities/default"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void updateSymbol_returnsUpdatedAbility() throws Exception {
        // arrange
        Ability ability = TestFixtures.ability(1, "fly");
        when(abilityService.updateSymbol(1, "fly")).thenReturn(ability);
        when(abilityMapper.toResponse(ability)).thenReturn(new AbilityResponse(1, "fly"));

        // act + assert
        mockMvc.perform(patch("/api/abilities/1/symbol")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("symbol", "fly"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("fly"));
    }

    @Test
    void updateSymbol_returns400WhenSymbolBlank() throws Exception {
        // arrange + act + assert
        mockMvc.perform(patch("/api/abilities/1/symbol")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("symbol", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    void delete_returns204() throws Exception {
        // arrange + act + assert
        mockMvc.perform(delete("/api/abilities/1"))
                .andExpect(status().isNoContent());
        verify(abilityService).deleteAbility(1);
    }

    @Test
    void getTranslations_returnsList() throws Exception {
        // arrange
        AbilityTranslation tr = new AbilityTranslation(Language.EN, "Static", "Paralyzes");
        when(abilityTranslationService.getAbilityTranslations(1)).thenReturn(List.of(tr));
        when(abilityMapper.toTranslationResponseList(List.of(tr)))
                .thenReturn(List.of(new AbilityTranslationResponse(1, Language.EN, "Static", "Paralyzes")));

        // act + assert
        mockMvc.perform(get("/api/abilities/1/translations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Static"));
    }

    @Test
    void saveTranslation_returnsTranslation() throws Exception {
        // arrange
        AbilityTranslation saved = new AbilityTranslation(Language.FR, "Statik", "Paralyse");
        when(abilityTranslationService.saveAbilityTranslation(eq(1), eq(Language.FR), eq("Statik"), eq("Paralyse")))
                .thenReturn(saved);
        when(abilityMapper.toTranslationResponse(saved))
                .thenReturn(new AbilityTranslationResponse(2, Language.FR, "Statik", "Paralyse"));

        // act + assert
        mockMvc.perform(put("/api/abilities/1/translations/FR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "Statik", "description", "Paralyse"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Statik"));
    }

    @Test
    void saveTranslation_returns400WhenNameBlank() throws Exception {
        // arrange + act + assert
        mockMvc.perform(put("/api/abilities/1/translations/FR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "", "description", "x"))))
                .andExpect(status().isBadRequest());
    }
}
