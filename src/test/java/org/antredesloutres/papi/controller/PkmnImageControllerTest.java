package org.antredesloutres.papi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.antredesloutres.papi.config.SecurityConfig;
import org.antredesloutres.papi.dto.response.PkmnImageResponse;
import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.antredesloutres.papi.exception.GlobalExceptionHandler;
import org.antredesloutres.papi.mapper.PkmnImageMapper;
import org.antredesloutres.papi.model.domain.Pkmn;
import org.antredesloutres.papi.model.domain.PkmnImage;
import org.antredesloutres.papi.security.JwtAuthFilter;
import org.antredesloutres.papi.service.domain.PkmnImageService;
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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PkmnImageController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, JwtAuthFilter.class}))
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class PkmnImageControllerTest {

    private static final Instant FIXED_TIME = Instant.parse("2024-01-01T00:00:00Z");

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    PkmnImageService pkmnImageService;
    @MockBean
    PkmnImageMapper pkmnImageMapper;

    private static PkmnImage entity(Long id, Pkmn pkmn, String url, boolean main) {
        PkmnImage img = new PkmnImage();
        img.setId(id);
        img.setPkmn(pkmn);
        img.setUrl(url);
        img.setMain(main);
        img.setAddedAt(FIXED_TIME);
        return img;
    }

    private static PkmnImageResponse response(Long id, String url, boolean main) {
        return new PkmnImageResponse(id, url, null, Set.of(), main, FIXED_TIME);
    }

    @Test
    void getImages_returnsList() throws Exception {
        // arrange
        PkmnImage img = entity(1L, TestFixtures.pkmn(25, "pikachu"), "https://x/a.png", false);
        when(pkmnImageService.getImages(25)).thenReturn(List.of(img));
        when(pkmnImageMapper.toResponseList(List.of(img)))
                .thenReturn(List.of(response(1L, "https://x/a.png", false)));

        // act + assert
        mockMvc.perform(get("/api/pokemon/25/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].url").value("https://x/a.png"));
    }

    @Test
    void getImages_withTagFilter_delegatesToTagLookup() throws Exception {
        // arrange
        PkmnImage img = entity(2L, TestFixtures.pkmn(25, "pikachu"), "https://x/b.png", false);
        when(pkmnImageService.getImagesByTag(25, "shiny")).thenReturn(List.of(img));
        when(pkmnImageMapper.toResponseList(List.of(img)))
                .thenReturn(List.of(response(2L, "https://x/b.png", false)));

        // act + assert
        mockMvc.perform(get("/api/pokemon/25/images").param("tag", "shiny"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));
        verify(pkmnImageService).getImagesByTag(25, "shiny");
    }

    @Test
    void getImages_withNameFilter_wrapsResultAsSingletonList() throws Exception {
        // arrange
        PkmnImage img = entity(3L, TestFixtures.pkmn(25, "pikachu"), "https://x/c.png", false);
        when(pkmnImageService.getImageByName(25, "front")).thenReturn(Optional.of(img));
        when(pkmnImageMapper.toResponse(img)).thenReturn(response(3L, "https://x/c.png", false));

        // act + assert
        mockMvc.perform(get("/api/pokemon/25/images").param("name", "front"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3));
    }

    @Test
    void getImages_withNameFilter_returnsEmptyListWhenMissing() throws Exception {
        // arrange
        when(pkmnImageService.getImageByName(25, "unknown")).thenReturn(Optional.empty());

        // act + assert
        mockMvc.perform(get("/api/pokemon/25/images").param("name", "unknown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getMainImage_returnsResponse() throws Exception {
        // arrange
        PkmnImage img = entity(1L, TestFixtures.pkmn(25, "pikachu"), "https://x/main.png", true);
        when(pkmnImageService.getMainImage(25)).thenReturn(Optional.of(img));
        when(pkmnImageMapper.toResponse(img)).thenReturn(response(1L, "https://x/main.png", true));

        // act + assert
        mockMvc.perform(get("/api/pokemon/25/images/main"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.main").value(true));
    }

    @Test
    void getMainImage_returns404WhenMissing() throws Exception {
        // arrange
        when(pkmnImageService.getMainImage(25)).thenReturn(Optional.empty());

        // act + assert
        mockMvc.perform(get("/api/pokemon/25/images/main"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void addImage_returns201() throws Exception {
        // arrange
        PkmnImage img = entity(10L, TestFixtures.pkmn(25, "pikachu"), "https://x/a.png", false);
        when(pkmnImageService.addImage(eq(25), any())).thenReturn(img);
        when(pkmnImageMapper.toResponse(img)).thenReturn(response(10L, "https://x/a.png", false));

        // act + assert
        mockMvc.perform(post("/api/pokemon/25/images")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("url", "https://x/a.png", "main", false))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void addImage_returns400WhenUrlBlank() throws Exception {
        // act + assert
        mockMvc.perform(post("/api/pokemon/25/images")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("url", "", "main", false))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    void addImage_returns404WhenPkmnMissing() throws Exception {
        // arrange
        when(pkmnImageService.addImage(eq(99), any()))
                .thenThrow(new EntityNotFoundException("Pokemon", 99));

        // act + assert
        mockMvc.perform(post("/api/pokemon/99/images")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("url", "https://x/a.png", "main", false))))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateImage_returnsUpdated() throws Exception {
        // arrange
        PkmnImage img = entity(10L, TestFixtures.pkmn(25, "pikachu"), "https://x/new.png", false);
        when(pkmnImageService.updateImage(eq(25), eq(10L), any())).thenReturn(img);
        when(pkmnImageMapper.toResponse(img)).thenReturn(response(10L, "https://x/new.png", false));

        // act + assert
        mockMvc.perform(patch("/api/pokemon/25/images/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("url", "https://x/new.png", "main", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://x/new.png"));
    }

    @Test
    void deleteImage_returns204() throws Exception {
        // act + assert
        mockMvc.perform(delete("/api/pokemon/25/images/10"))
                .andExpect(status().isNoContent());
        verify(pkmnImageService).deleteImage(25, 10L);
    }

    @Test
    void promoteToMain_returnsUpdated() throws Exception {
        // arrange
        PkmnImage img = entity(10L, TestFixtures.pkmn(25, "pikachu"), "https://x/a.png", true);
        when(pkmnImageService.promoteToMain(25, 10L)).thenReturn(img);
        when(pkmnImageMapper.toResponse(img)).thenReturn(response(10L, "https://x/a.png", true));

        // act + assert
        mockMvc.perform(post("/api/pokemon/25/images/10/main"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.main").value(true));
    }
}
