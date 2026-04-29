package org.antredesloutres.papi.service.domain;

import org.antredesloutres.papi.dto.domain.UserCreateRequest;
import org.antredesloutres.papi.dto.domain.UserUpdateRequest;
import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.antredesloutres.papi.model.domain.User;
import org.antredesloutres.papi.repository.UserRepository;
import org.antredesloutres.papi.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService service;

    @Test
    void getAllUsers_returnsPage() {
        // arrange
        Page<User> page = new PageImpl<>(List.of(TestFixtures.user(1L, "alice", "ROLE_USER")));
        when(userRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);

        // act
        Page<User> result = service.getAllUsers(PageRequest.of(0, 10));

        // assert
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getUserById_returnsUser() {
        // arrange
        User user = TestFixtures.user(1L, "alice", "ROLE_USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // act
        User result = service.getUserById(1L);

        // assert
        assertThat(result).isSameAs(user);
    }

    @Test
    void getUserById_throwsWhenMissing() {
        // arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> service.getUserById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createUser_persistsHashedPasswordAndDefaultRole() {
        // arrange
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("strongpass")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // act
        User result = service.createUser(new UserCreateRequest("alice", "strongpass", null));

        // assert
        assertThat(result.getUsername()).isEqualTo("alice");
        assertThat(result.getPassword()).isEqualTo("hashed");
        assertThat(result.getRole()).isEqualTo("ROLE_USER");
    }

    @Test
    void createUser_acceptsRoleWithoutPrefix() {
        // arrange
        when(userRepository.findByUsername("bob")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("strongpass")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // act
        User result = service.createUser(new UserCreateRequest("bob", "strongpass", "ADMIN"));

        // assert
        assertThat(result.getRole()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void createUser_throwsOnInvalidRole() {
        // arrange
        when(userRepository.findByUsername("bob")).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> service.createUser(new UserCreateRequest("bob", "strongpass", "WIZARD")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid role");
    }

    @Test
    void createUser_throwsWhenUsernameAlreadyExists() {
        // arrange
        when(userRepository.findByUsername("alice"))
                .thenReturn(Optional.of(TestFixtures.user(1L, "alice", "ROLE_USER")));

        // act + assert
        assertThatThrownBy(() -> service.createUser(new UserCreateRequest("alice", "strongpass", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void updateUser_changesUsernameAndPasswordAndRole() {
        // arrange
        User existing = TestFixtures.user(1L, "alice", "ROLE_USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findByUsername("alice2")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("newstrongpass")).thenReturn("new-hash");
        when(userRepository.save(existing)).thenReturn(existing);

        // act
        User result = service.updateUser(1L, new UserUpdateRequest("alice2", "newstrongpass", "ADMIN"));

        // assert
        assertThat(result.getUsername()).isEqualTo("alice2");
        assertThat(result.getPassword()).isEqualTo("new-hash");
        assertThat(result.getRole()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void updateUser_keepsPasswordWhenNullOrBlank() {
        // arrange
        User existing = TestFixtures.user(1L, "alice", "ROLE_USER");
        String oldHash = existing.getPassword();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        // act
        User result = service.updateUser(1L, new UserUpdateRequest(null, null, null));

        // assert
        assertThat(result.getPassword()).isEqualTo(oldHash);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void updateUser_throwsWhenUsernameTakenByAnotherUser() {
        // arrange
        User existing = TestFixtures.user(1L, "alice", "ROLE_USER");
        User other = TestFixtures.user(2L, "bob", "ROLE_USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(other));

        // act + assert
        assertThatThrownBy(() -> service.updateUser(1L, new UserUpdateRequest("bob", null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void updateUser_allowsRenameToSelfUsername() {
        // arrange
        User existing = TestFixtures.user(1L, "alice", "ROLE_USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        // act
        User result = service.updateUser(1L, new UserUpdateRequest("alice", null, null));

        // assert
        assertThat(result.getUsername()).isEqualTo("alice");
    }

    @Test
    void updateUser_throwsWhenUserMissing() {
        // arrange
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> service.updateUser(404L, new UserUpdateRequest(null, null, null)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteUser_deletesExisting() {
        // arrange
        when(userRepository.existsById(1L)).thenReturn(true);

        // act
        service.deleteUser(1L);

        // assert
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_throwsWhenMissing() {
        // arrange
        when(userRepository.existsById(404L)).thenReturn(false);

        // act + assert
        assertThatThrownBy(() -> service.deleteUser(404L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createUser_persistsExpectedFields() {
        // arrange
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("strongpass")).thenReturn("hashed");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        // act
        service.createUser(new UserCreateRequest("alice", "strongpass", "ROLE_USER"));

        // assert
        User saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("alice");
        assertThat(saved.getPassword()).isEqualTo("hashed");
        assertThat(saved.getRole()).isEqualTo("ROLE_USER");
    }
}
