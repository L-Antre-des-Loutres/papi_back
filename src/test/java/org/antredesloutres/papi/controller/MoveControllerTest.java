package org.antredesloutres.papi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.antredesloutres.papi.config.SecurityConfig;
import org.antredesloutres.papi.dto.response.MoveResponse;
import org.antredesloutres.papi.dto.response.MoveTranslationResponse;
import org.antredesloutres.papi.exception.GlobalExceptionHandler;
import org.antredesloutres.papi.mapper.MoveMapper;
import org.antredesloutres.papi.model.domain.Move;
import org.antredesloutres.papi.model.enumerated.Language;
import org.antredesloutres.papi.model.translation.MoveTranslation;
import org.antredesloutres.papi.security.JwtAuthFilter;
import org.antredesloutres.papi.service.domain.MoveService;
import org.antredesloutres.papi.service.translation.MoveTranslationService;
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

import java.util.HashMap;
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

@WebMvcTest(controllers = MoveController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, JwtAuthFilter.class}))
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class MoveControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    MoveService moveService;
    @MockBean
    MoveTranslationService moveTranslationService;
    @MockBean
    MoveMapper moveMapper;

    @Test
    void getAll_returnsPage() throws Exception {
        // arrange
        Move move = TestFixtures.move(1, "tackle", null);
        when(moveService.getAllMoves(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(move)));
        when(moveMapper.toResponse(move)).thenReturn(new MoveResponse(1, "tackle", null, 80, 100, 15));

        // act + assert
        mockMvc.perform(get("/api/moves"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].symbol").value("tackle"));
    }

    @Test
    void getById_returnsMove() throws Exception {
        // arrange
        Move move = TestFixtures.move(1, "tackle", null);
        when(moveService.getMoveById(1)).thenReturn(move);
        when(moveMapper.toResponse(move)).thenReturn(new MoveResponse(1, "tackle", null, 80, 100, 15));

        // act + assert
        mockMvc.perform(get("/api/moves/1"))
                .andExpect(status().isOk());
    }

    @Test
    void createDefault_returns201() throws Exception {
        // arrange
        Move move = TestFixtures.move(2, "new-move-abc", null);
        when(moveService.createDefaultMove(any(), any(), any(), any(), any(), any())).thenReturn(move);
        when(moveMapper.toResponse(move)).thenReturn(new MoveResponse(2, "new-move-abc", null, 0, 0, 0));

        // act + assert
        mockMvc.perform(post("/api/moves/default"))
                .andExpect(status().isCreated());
    }

    @Test
    void updateSymbol_returnsMove() throws Exception {
        // arrange
        Move move = TestFixtures.move(1, "ember", null);
        when(moveService.updateSymbol(1, "ember")).thenReturn(move);
        when(moveMapper.toResponse(move)).thenReturn(new MoveResponse(1, "ember", null, 80, 100, 15));

        // act + assert
        mockMvc.perform(patch("/api/moves/1/symbol")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("symbol", "ember"))))
                .andExpect(status().isOk());
    }

    @Test
    void updateType_acceptsNullTypeId() throws Exception {
        // arrange
        Move move = TestFixtures.move(1, "ember", null);
        when(moveService.updateType(1, null)).thenReturn(move);
        when(moveMapper.toResponse(move)).thenReturn(new MoveResponse(1, "ember", null, 80, 100, 15));

        Map<String, Object> body = new HashMap<>();
        body.put("typeId", null);

        // act + assert
        mockMvc.perform(patch("/api/moves/1/type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void updatePower_returnsMove() throws Exception {
        // arrange
        Move move = TestFixtures.move(1, "tackle", null);
        when(moveService.updatePower(eq(1), eq(120))).thenReturn(move);
        when(moveMapper.toResponse(move)).thenReturn(new MoveResponse(1, "tackle", null, 120, 100, 15));

        // act + assert
        mockMvc.perform(patch("/api/moves/1/power")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("value", 120))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.power").value(120));
    }

    @Test
    void updatePower_returns400WhenValueMissing() throws Exception {
        // arrange + act + assert
        mockMvc.perform(patch("/api/moves/1/power")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateAccuracy_returnsMove() throws Exception {
        // arrange
        Move move = TestFixtures.move(1, "tackle", null);
        when(moveService.updateAccuracy(1, 95)).thenReturn(move);
        when(moveMapper.toResponse(move)).thenReturn(new MoveResponse(1, "tackle", null, 80, 95, 15));

        // act + assert
        mockMvc.perform(patch("/api/moves/1/accuracy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("value", 95))))
                .andExpect(status().isOk());
    }

    @Test
    void updatePp_returnsMove() throws Exception {
        // arrange
        Move move = TestFixtures.move(1, "tackle", null);
        when(moveService.updatePp(1, 5)).thenReturn(move);
        when(moveMapper.toResponse(move)).thenReturn(new MoveResponse(1, "tackle", null, 80, 100, 5));

        // act + assert
        mockMvc.perform(patch("/api/moves/1/pp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("value", 5))))
                .andExpect(status().isOk());
    }

    @Test
    void delete_returns204() throws Exception {
        // arrange + act + assert
        mockMvc.perform(delete("/api/moves/1"))
                .andExpect(status().isNoContent());
        verify(moveService).deleteMove(1);
    }

    @Test
    void getTranslations_returnsList() throws Exception {
        // arrange
        MoveTranslation tr = new MoveTranslation(Language.EN, "Tackle", "Charge");
        when(moveTranslationService.getMoveTranslations(1)).thenReturn(List.of(tr));
        when(moveMapper.toTranslationResponseList(List.of(tr)))
                .thenReturn(List.of(new MoveTranslationResponse(1, Language.EN, "Tackle", "Charge")));

        // act + assert
        mockMvc.perform(get("/api/moves/1/translations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Tackle"));
    }

    @Test
    void saveTranslation_returnsTranslation() throws Exception {
        // arrange
        MoveTranslation saved = new MoveTranslation(Language.FR, "Charge", "Charge fr");
        when(moveTranslationService.saveMoveTranslation(eq(1), eq(Language.FR), eq("Charge"), eq("Charge fr")))
                .thenReturn(saved);
        when(moveMapper.toTranslationResponse(saved))
                .thenReturn(new MoveTranslationResponse(1, Language.FR, "Charge", "Charge fr"));

        // act + assert
        mockMvc.perform(put("/api/moves/1/translations/FR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "Charge", "description", "Charge fr"))))
                .andExpect(status().isOk());
    }
}
