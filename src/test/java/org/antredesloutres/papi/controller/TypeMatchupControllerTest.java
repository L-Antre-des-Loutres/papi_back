package org.antredesloutres.papi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.antredesloutres.papi.config.SecurityConfig;
import org.antredesloutres.papi.dto.domain.TypeMatchupDto;
import org.antredesloutres.papi.dto.response.TypeMatchupResponse;
import org.antredesloutres.papi.exception.GlobalExceptionHandler;
import org.antredesloutres.papi.mapper.TypeMatchupMapper;
import org.antredesloutres.papi.security.JwtAuthFilter;
import org.antredesloutres.papi.service.domain.TypeMatchupService;
import org.antredesloutres.papi.model.domain.TypeMatchup;
import org.antredesloutres.papi.model.enumerated.Effectiveness;
import org.antredesloutres.papi.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TypeMatchupController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, JwtAuthFilter.class}))
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class TypeMatchupControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    TypeMatchupService matchupService;
    @MockBean
    TypeMatchupMapper matchupMapper;

    @Test
    void getAll_returnsList() throws Exception {
        // arrange
        TypeMatchup m = TestFixtures.matchup(1L,
                TestFixtures.type(1, "fire"), TestFixtures.type(2, "water"),
                Effectiveness.NOT_VERY_EFFECTIVE);
        when(matchupService.findAll()).thenReturn(List.of(m));
        when(matchupMapper.toResponseList(List.of(m))).thenReturn(List.of(
                new TypeMatchupResponse(1L, null, null, Effectiveness.NOT_VERY_EFFECTIVE)));

        // act + assert
        mockMvc.perform(get("/api/types/matchups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].effectiveness").value("NOT_VERY_EFFECTIVE"));
    }

    @Test
    void upsert_returns204() throws Exception {
        // arrange + act + assert
        mockMvc.perform(put("/api/types/matchups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "attackingTypeId", 1,
                                "defendingTypeId", 2,
                                "effectiveness", "SUPER_EFFECTIVE"))))
                .andExpect(status().isNoContent());
        verify(matchupService).upsert(new TypeMatchupDto(1, 2, Effectiveness.SUPER_EFFECTIVE));
    }

    @Test
    void upsert_returns400WhenAttackerIdMissing() throws Exception {
        // arrange
        Map<String, Object> body = new HashMap<>();
        body.put("defendingTypeId", 2);
        body.put("effectiveness", "SUPER_EFFECTIVE");

        // act + assert
        mockMvc.perform(put("/api/types/matchups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upsert_returns400WhenEffectivenessInvalid() throws Exception {
        // arrange + act + assert
        mockMvc.perform(put("/api/types/matchups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "attackingTypeId", 1,
                                "defendingTypeId", 2,
                                "effectiveness", "BANANA"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reset_returns204() throws Exception {
        // arrange + act + assert
        mockMvc.perform(delete("/api/types/matchups/1/2"))
                .andExpect(status().isNoContent());
        verify(matchupService).reset(1, 2);
    }
}
