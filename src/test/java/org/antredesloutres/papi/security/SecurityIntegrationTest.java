package org.antredesloutres.papi.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @WithAnonymousUser
    void anonymousAccessToProtectedEndpointReturns401() throws Exception {
        // arrange + act + assert
        mockMvc.perform(get("/api/pokemon"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    void anonymousAccessToHealthEndpointReturns200() throws Exception {
        // arrange + act + assert
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void loginEndpointIsPublic() throws Exception {
        // arrange + act + assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"x\",\"password\":\"y\"}"))
                .andExpect(status().is(org.springframework.http.HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    @WithMockUser(roles = "USER")
    void userRoleCanAccessNonAdminEndpoint() throws Exception {
        // arrange + act + assert
        mockMvc.perform(get("/api/pokemon"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userRoleCannotAccessUsersEndpoint() throws Exception {
        // arrange + act + assert
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminRoleCanAccessUsersEndpoint() throws Exception {
        // arrange + act + assert
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void swaggerEndpointIsPublic() throws Exception {
        // arrange + act + assert
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }
}
