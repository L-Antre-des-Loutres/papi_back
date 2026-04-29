package org.antredesloutres.papi.controller;

import org.antredesloutres.papi.config.SecurityConfig;
import org.antredesloutres.papi.dto.response.MovesetResponse;
import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.antredesloutres.papi.exception.GlobalExceptionHandler;
import org.antredesloutres.papi.mapper.MovesetMapper;
import org.antredesloutres.papi.model.domain.Moveset;
import org.antredesloutres.papi.model.enumerated.MoveLearnMethod;
import org.antredesloutres.papi.security.JwtAuthFilter;
import org.antredesloutres.papi.service.domain.MovesetService;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MovesetController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, JwtAuthFilter.class}))
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class MovesetControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    MovesetService movesetService;
    @MockBean
    MovesetMapper movesetMapper;

    @Test
    void getAll_returnsPage() throws Exception {
        // arrange
        Moveset entry = TestFixtures.moveset(1, TestFixtures.pkmn(25, "pikachu"), TestFixtures.move(1, "x", null));
        when(movesetService.getAllMovesets(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entry)));
        when(movesetMapper.toResponse(entry)).thenReturn(new MovesetResponse(1, 25, null, MoveLearnMethod.LEVEL_UP, 10));

        // act + assert
        mockMvc.perform(get("/api/movesets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].pkmnId").value(25));
    }

    @Test
    void getById_returnsMoveset() throws Exception {
        // arrange
        Moveset entry = TestFixtures.moveset(1, TestFixtures.pkmn(25, "pikachu"), TestFixtures.move(1, "x", null));
        when(movesetService.getMovesetById(1)).thenReturn(entry);
        when(movesetMapper.toResponse(entry)).thenReturn(new MovesetResponse(1, 25, null, MoveLearnMethod.LEVEL_UP, 10));

        // act + assert
        mockMvc.perform(get("/api/movesets/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getById_returns404WhenMissing() throws Exception {
        // arrange
        when(movesetService.getMovesetById(99)).thenThrow(new EntityNotFoundException("Moveset", 99));

        // act + assert
        mockMvc.perform(get("/api/movesets/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByPkmnId_returnsList() throws Exception {
        // arrange
        Moveset entry = TestFixtures.moveset(1, TestFixtures.pkmn(25, "pikachu"), TestFixtures.move(1, "x", null));
        when(movesetService.getPkmnMovesetByPkmnId(25)).thenReturn(List.of(entry));
        when(movesetMapper.toResponseList(List.of(entry))).thenReturn(List.of(
                new MovesetResponse(1, 25, null, MoveLearnMethod.LEVEL_UP, 10)));

        // act + assert
        mockMvc.perform(get("/api/movesets/pkmn/25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pkmnId").value(25));
    }
}
