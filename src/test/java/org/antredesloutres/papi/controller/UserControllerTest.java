package org.antredesloutres.papi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.antredesloutres.papi.config.SecurityConfig;
import org.antredesloutres.papi.dto.domain.UserCreateRequest;
import org.antredesloutres.papi.dto.domain.UserUpdateRequest;
import org.antredesloutres.papi.exception.GlobalExceptionHandler;
import org.antredesloutres.papi.model.domain.User;
import org.antredesloutres.papi.security.JwtAuthFilter;
import org.antredesloutres.papi.service.domain.UserService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, JwtAuthFilter.class}))
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserService userService;

    @Test
    void getAll_returnsPageOfUserResponses() throws Exception {
        // arrange
        User user = TestFixtures.user(1L, "alice", "ROLE_USER");
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(user)));

        // act + assert
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("alice"))
                .andExpect(jsonPath("$.content[0].password").doesNotExist());
    }

    @Test
    void getById_returnsUserWithoutPassword() throws Exception {
        // arrange
        User user = TestFixtures.user(1L, "alice", "ROLE_USER");
        when(userService.getUserById(1L)).thenReturn(user);

        // act + assert
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void create_returns201() throws Exception {
        // arrange
        User user = TestFixtures.user(2L, "bob", "ROLE_USER");
        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(user);

        // act + assert
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "bob",
                                "password", "strongpass",
                                "role", "ROLE_USER"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("bob"));
    }

    @Test
    void create_returns400WhenUsernameTooShort() throws Exception {
        // arrange + act + assert
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "ab",
                                "password", "strongpass"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_returns400WhenPasswordTooShort() throws Exception {
        // arrange + act + assert
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "alice",
                                "password", "short"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_returns400WhenUsernameMissing() throws Exception {
        // arrange + act + assert
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("password", "strongpass"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_returns200() throws Exception {
        // arrange
        User user = TestFixtures.user(1L, "alice", "ROLE_ADMIN");
        when(userService.updateUser(eq(1L), any(UserUpdateRequest.class))).thenReturn(user);

        // act + assert
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", "ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    @Test
    void update_acceptsEmptyBody() throws Exception {
        // arrange
        User user = TestFixtures.user(1L, "alice", "ROLE_USER");
        when(userService.updateUser(eq(1L), any(UserUpdateRequest.class))).thenReturn(user);

        // act + assert
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void delete_returns204() throws Exception {
        // arrange + act + assert
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
        verify(userService).deleteUser(1L);
    }
}
