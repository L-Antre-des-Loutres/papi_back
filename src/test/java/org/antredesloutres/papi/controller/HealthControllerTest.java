package org.antredesloutres.papi.controller;

import org.antredesloutres.papi.config.SecurityConfig;
import org.antredesloutres.papi.security.JwtAuthFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HealthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, JwtAuthFilter.class}))
@AutoConfigureMockMvc(addFilters = false)
class HealthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void rootReturnsHealthMessage() throws Exception {
        // arrange + act + assert
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string("API up and running"));
    }

    @Test
    void healthEndpointReturnsHealthMessage() throws Exception {
        // arrange + act + assert
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("API up and running"));
    }
}
