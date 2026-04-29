package org.antredesloutres.papi.repository;

import org.antredesloutres.papi.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Test
    void findByUsername_returnsUserWhenPresent() {
        // arrange
        User saved = userRepository.save(User.builder()
                .username("alice")
                .password("hash")
                .role("ROLE_USER")
                .build());

        // act
        Optional<User> result = userRepository.findByUsername("alice");

        // assert
        assertThat(result).isPresent().get().extracting(User::getId).isEqualTo(saved.getId());
    }

    @Test
    void findByUsername_returnsEmptyWhenAbsent() {
        // arrange + act
        Optional<User> result = userRepository.findByUsername("ghost");

        // assert
        assertThat(result).isEmpty();
    }
}
