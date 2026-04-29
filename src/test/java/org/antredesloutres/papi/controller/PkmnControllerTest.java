package org.antredesloutres.papi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.antredesloutres.papi.config.SecurityConfig;
import org.antredesloutres.papi.dto.domain.PkmnUpdateDto;
import org.antredesloutres.papi.dto.response.MovesetResponse;
import org.antredesloutres.papi.dto.response.PkmnResponse;
import org.antredesloutres.papi.dto.response.PkmnSummaryResponse;
import org.antredesloutres.papi.dto.response.PkmnTranslationResponse;
import org.antredesloutres.papi.exception.GlobalExceptionHandler;
import org.antredesloutres.papi.mapper.MovesetMapper;
import org.antredesloutres.papi.mapper.PkmnMapper;
import org.antredesloutres.papi.model.domain.Move;
import org.antredesloutres.papi.model.domain.Moveset;
import org.antredesloutres.papi.model.domain.Pkmn;
import org.antredesloutres.papi.model.enumerated.Language;
import org.antredesloutres.papi.model.enumerated.MoveLearnMethod;
import org.antredesloutres.papi.model.translation.PkmnTranslation;
import org.antredesloutres.papi.security.JwtAuthFilter;
import org.antredesloutres.papi.service.domain.MovesetService;
import org.antredesloutres.papi.service.domain.PkmnService;
import org.antredesloutres.papi.service.translation.PkmnTranslationService;
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

@WebMvcTest(controllers = PkmnController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, JwtAuthFilter.class}))
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class PkmnControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    PkmnService pkmnService;
    @MockBean
    PkmnTranslationService pkmnTranslationService;
    @MockBean
    MovesetService movesetService;
    @MockBean
    PkmnMapper pkmnMapper;
    @MockBean
    MovesetMapper movesetMapper;

    @Test
    void getAll_returnsPagedSummaries() throws Exception {
        // arrange
        Pkmn p = TestFixtures.pkmn(25, "pikachu");
        when(pkmnService.getAllPkmn(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));
        when(pkmnMapper.toSummary(p)).thenReturn(new PkmnSummaryResponse(25, "pikachu", 25, null, null));

        // act + assert
        mockMvc.perform(get("/api/pokemon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].symbol").value("pikachu"));
    }

    @Test
    void getById_returnsFullResponse() throws Exception {
        // arrange
        Pkmn p = TestFixtures.pkmn(25, "pikachu");
        when(pkmnService.getPkmnById(25)).thenReturn(p);
        when(pkmnMapper.toResponse(p)).thenReturn(buildPikachuResponse());

        // act + assert
        mockMvc.perform(get("/api/pokemon/25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(25));
    }

    @Test
    void createDefault_returns201() throws Exception {
        // arrange
        Pkmn p = TestFixtures.pkmn(99, "new-pkmn-abc");
        when(pkmnService.createDefaultPkmn(any())).thenReturn(p);
        when(pkmnMapper.toResponse(p)).thenReturn(buildPikachuResponse());

        // act + assert
        mockMvc.perform(post("/api/pokemon/default"))
                .andExpect(status().isCreated());
    }

    @Test
    void updateAll_acceptsValidPayload() throws Exception {
        // arrange
        Pkmn p = TestFixtures.pkmn(25, "pikachu");
        when(pkmnService.updateAll(eq(25), any(PkmnUpdateDto.class))).thenReturn(p);
        when(pkmnMapper.toResponse(p)).thenReturn(buildPikachuResponse());

        // act + assert
        mockMvc.perform(patch("/api/pokemon/25")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("baseHp", 35, "baseSpeed", 90))))
                .andExpect(status().isOk());
    }

    @Test
    void updateAll_returns400WhenStatOutOfRange() throws Exception {
        // arrange + act + assert
        mockMvc.perform(patch("/api/pokemon/25")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("baseHp", 9999))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    void updateAll_returns400WhenEvOutOfRange() throws Exception {
        // arrange + act + assert
        mockMvc.perform(patch("/api/pokemon/25")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("evHp", 5))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSymbol_returns200() throws Exception {
        // arrange
        Pkmn p = TestFixtures.pkmn(25, "raichu");
        when(pkmnService.updateSymbol(25, "raichu")).thenReturn(p);
        when(pkmnMapper.toResponse(p)).thenReturn(buildPikachuResponse());

        // act + assert
        mockMvc.perform(patch("/api/pokemon/25/symbol")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("symbol", "raichu"))))
                .andExpect(status().isOk());
    }

    @Test
    void updateNationalDex_returns200() throws Exception {
        // arrange
        Pkmn p = TestFixtures.pkmn(25, "pikachu");
        when(pkmnService.updateNationalDexNumber(25, 25)).thenReturn(p);
        when(pkmnMapper.toResponse(p)).thenReturn(buildPikachuResponse());

        // act + assert
        mockMvc.perform(patch("/api/pokemon/25/nationalDexNumber")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("value", 25))))
                .andExpect(status().isOk());
    }

    @Test
    void updatePrimaryType_acceptsTypeId() throws Exception {
        // arrange
        Pkmn p = TestFixtures.pkmn(25, "pikachu");
        when(pkmnService.updatePrimaryType(25, 13)).thenReturn(p);
        when(pkmnMapper.toResponse(p)).thenReturn(buildPikachuResponse());

        // act + assert
        mockMvc.perform(patch("/api/pokemon/25/primaryType")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("typeId", 13))))
                .andExpect(status().isOk());
    }

    @Test
    void updateTags_returnsPkmn() throws Exception {
        // arrange
        Pkmn p = TestFixtures.pkmn(25, "pikachu");
        when(pkmnService.updateTags(eq(25), any())).thenReturn(p);
        when(pkmnMapper.toResponse(p)).thenReturn(buildPikachuResponse());

        // act + assert
        mockMvc.perform(patch("/api/pokemon/25/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("tags", List.of("rare")))))
                .andExpect(status().isOk());
    }

    @Test
    void delete_returns204() throws Exception {
        // arrange + act + assert
        mockMvc.perform(delete("/api/pokemon/25"))
                .andExpect(status().isNoContent());
        verify(pkmnService).deletePkmn(25);
    }

    @Test
    void getTranslations_returnsList() throws Exception {
        // arrange
        PkmnTranslation tr = new PkmnTranslation(Language.EN, "Pikachu", "Normal", "Mouse pkmn");
        when(pkmnTranslationService.getPkmnTranslations(25)).thenReturn(List.of(tr));
        when(pkmnMapper.toTranslationResponseList(List.of(tr))).thenReturn(List.of(
                new PkmnTranslationResponse(1, Language.EN, "Pikachu", "Normal", "Mouse pkmn")));

        // act + assert
        mockMvc.perform(get("/api/pokemon/25/translations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pkmnName").value("Pikachu"));
    }

    @Test
    void saveTranslation_returnsTranslation() throws Exception {
        // arrange
        PkmnTranslation saved = new PkmnTranslation(Language.FR, "Pikachu", "Normal", "Souris");
        when(pkmnTranslationService.savePkmnTranslation(eq(25), eq(Language.FR), any(), any(), any()))
                .thenReturn(saved);
        when(pkmnMapper.toTranslationResponse(saved))
                .thenReturn(new PkmnTranslationResponse(1, Language.FR, "Pikachu", "Normal", "Souris"));

        // act + assert
        mockMvc.perform(put("/api/pokemon/25/translations/FR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "pkmnName", "Pikachu",
                                "formName", "Normal",
                                "description", "Souris"))))
                .andExpect(status().isOk());
    }

    @Test
    void getMoveset_returnsList() throws Exception {
        // arrange
        Pkmn p = TestFixtures.pkmn(25, "pikachu");
        Move m = TestFixtures.move(1, "thunderbolt", null);
        Moveset entry = TestFixtures.moveset(1, p, m);
        when(movesetService.getPkmnMovesetByPkmnId(25)).thenReturn(List.of(entry));
        when(movesetMapper.toResponseList(List.of(entry))).thenReturn(List.of(
                new MovesetResponse(1, 25, null, MoveLearnMethod.LEVEL_UP, 26)));

        // act + assert
        mockMvc.perform(get("/api/pokemon/25/moveset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pkmnId").value(25));
    }

    @Test
    void addMove_returns201() throws Exception {
        // arrange
        Pkmn p = TestFixtures.pkmn(25, "pikachu");
        Moveset entry = TestFixtures.moveset(1, p, TestFixtures.move(1, "x", null));
        when(movesetService.addMove(eq(25), any())).thenReturn(entry);
        when(movesetMapper.toResponse(entry)).thenReturn(
                new MovesetResponse(1, 25, null, MoveLearnMethod.LEVEL_UP, 26));

        // act + assert
        mockMvc.perform(post("/api/pokemon/25/moveset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "moveId", 1,
                                "learnMethod", "LEVEL_UP",
                                "learnLevel", 26))))
                .andExpect(status().isCreated());
    }

    @Test
    void addMove_returns400WhenMoveIdMissing() throws Exception {
        // arrange + act + assert
        mockMvc.perform(post("/api/pokemon/25/moveset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "learnMethod", "LEVEL_UP",
                                "learnLevel", 26))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addMove_returns400WhenLearnLevelLessThanOne() throws Exception {
        // arrange + act + assert
        mockMvc.perform(post("/api/pokemon/25/moveset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "moveId", 1,
                                "learnMethod", "LEVEL_UP",
                                "learnLevel", 0))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteMove_returns204() throws Exception {
        // arrange + act + assert
        mockMvc.perform(delete("/api/pokemon/25/moveset/1"))
                .andExpect(status().isNoContent());
        verify(movesetService).deleteMove(25, 1);
    }

    private static PkmnResponse buildPikachuResponse() {
        return new PkmnResponse(
                25, "pikachu", 25, null, null, null, null, null,
                Set.of(), 40, 60,
                35, 55, 40, 50, 50, 90,
                0, 0, 0, 0, 0, 2,
                112, null, 190, 50, 10, Set.of(), 70);
    }
}
