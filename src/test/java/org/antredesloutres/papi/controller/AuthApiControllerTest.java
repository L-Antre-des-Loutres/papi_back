package org.antredesloutres.papi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.antredesloutres.papi.config.SecurityConfig;
import org.antredesloutres.papi.exception.GlobalExceptionHandler;
import org.antredesloutres.papi.security.JwtAuthFilter;
import org.antredesloutres.papi.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthApiController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, JwtAuthFilter.class}))
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthApiControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    AuthenticationManager authenticationManager;
    @MockBean
    JwtUtils jwtUtils;

    @Test
    void login_returnsToken() throws Exception {
        // arrange
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "alice", "pwd", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtUtils.generateToken(any(), any())).thenReturn("fake-jwt-token");

        // act + assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "alice",
                                "password", "pwd"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"));
    }

    @Test
    void login_returns401OnBadCredentials() throws Exception {
        // arrange
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad creds"));

        // act + assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "alice",
                                "password", "wrong"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_returns400WhenUsernameBlank() throws Exception {
        // arrange + act + assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "",
                                "password", "pwd"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_returns400WhenPasswordMissing() throws Exception {
        // arrange + act + assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "alice"))))
                .andExpect(status().isBadRequest());
    }
}
