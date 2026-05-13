package org.antredesloutres.papi.controller;

import org.antredesloutres.papi.config.SecurityConfig;
import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.antredesloutres.papi.security.JwtAuthFilter;
import org.antredesloutres.papi.service.image.ImageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ImageController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, JwtAuthFilter.class}))
@AutoConfigureMockMvc(addFilters = false)
class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageService imageService;

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
}
