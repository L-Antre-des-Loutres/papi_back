package org.antredesloutres.papi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    JwtUtils jwtUtils;
    @Mock
    UserDetailsService userDetailsService;
    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;
    @Mock
    FilterChain chain;

    @InjectMocks
    JwtAuthFilter filter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_setsAuthenticationWhenTokenValid() throws Exception {
        // arrange
        UserDetails user = User.withUsername("alice")
                .password("hash")
                .roles("USER")
                .build();
        when(request.getHeader("Authorization")).thenReturn("Bearer good-token");
        when(jwtUtils.validateToken("good-token")).thenReturn(true);
        when(jwtUtils.getUsernameFromToken("good-token")).thenReturn("alice");
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(user);

        // act
        filter.doFilterInternal(request, response, chain);

        // assert
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo("alice");
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_skipsWhenNoAuthorizationHeader() throws Exception {
        // arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // act
        filter.doFilterInternal(request, response, chain);

        // assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
        verify(userDetailsService, never()).loadUserByUsername(any());
    }

    @Test
    void doFilterInternal_skipsWhenHeaderIsNotBearer() throws Exception {
        // arrange
        when(request.getHeader("Authorization")).thenReturn("Basic abc123");

        // act
        filter.doFilterInternal(request, response, chain);

        // assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtUtils, never()).validateToken(any());
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_doesNotAuthenticateWhenTokenInvalid() throws Exception {
        // arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer bad-token");
        when(jwtUtils.validateToken("bad-token")).thenReturn(false);

        // act
        filter.doFilterInternal(request, response, chain);

        // assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(userDetailsService, never()).loadUserByUsername(any());
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_propagatesAuthorities() throws Exception {
        // arrange
        UserDetails user = User.withUsername("admin")
                .password("hash")
                .roles("ADMIN")
                .build();
        when(request.getHeader("Authorization")).thenReturn("Bearer t");
        when(jwtUtils.validateToken("t")).thenReturn(true);
        when(jwtUtils.getUsernameFromToken("t")).thenReturn("admin");
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(user);

        // act
        filter.doFilterInternal(request, response, chain);

        // assert
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getAuthorities())
                .extracting(a -> a.getAuthority())
                .containsExactly("ROLE_ADMIN");
        assertThat(auth.getAuthorities()).isNotEqualTo(Collections.emptyList());
    }
}
