package org.antredesloutres.papi.security;

import org.antredesloutres.papi.model.domain.User;
import org.antredesloutres.papi.repository.UserRepository;
import org.antredesloutres.papi.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserDetailsServiceImpl service;

    @Test
    void loadUserByUsername_returnsUserDetails() {
        // arrange
        User user = TestFixtures.user(1L, "alice", "ROLE_USER");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        // act
        UserDetails result = service.loadUserByUsername("alice");

        // assert
        assertThat(result.getUsername()).isEqualTo("alice");
        assertThat(result.getAuthorities()).extracting(a -> a.getAuthority()).containsExactly("ROLE_USER");
    }

    @Test
    void loadUserByUsername_mapsAdminRoleCorrectly() {
        // arrange
        User user = TestFixtures.user(1L, "root", "ROLE_ADMIN");
        when(userRepository.findByUsername("root")).thenReturn(Optional.of(user));

        // act
        UserDetails result = service.loadUserByUsername("root");

        // assert
        assertThat(result.getAuthorities()).extracting(a -> a.getAuthority()).containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_throwsWhenUserNotFound() {
        // arrange
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> service.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("ghost");
    }
}
