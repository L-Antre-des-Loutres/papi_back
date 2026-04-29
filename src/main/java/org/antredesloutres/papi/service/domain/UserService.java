package org.antredesloutres.papi.service.domain;

import lombok.RequiredArgsConstructor;
import org.antredesloutres.papi.dto.domain.UserCreateRequest;
import org.antredesloutres.papi.dto.domain.UserUpdateRequest;
import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.antredesloutres.papi.model.domain.User;
import org.antredesloutres.papi.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Set<String> ALLOWED_ROLES = Set.of("ROLE_USER", "ROLE_ADMIN");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));
    }

    @Transactional
    public User createUser(UserCreateRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + request.username());
        }

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(normalizeRole(request.role()))
                .build();
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, UserUpdateRequest request) {
        User user = getUserById(id);

        if (request.username() != null && !request.username().isBlank()
                && !request.username().equals(user.getUsername())) {
            userRepository.findByUsername(request.username()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("Username already exists: " + request.username());
                }
            });
            user.setUsername(request.username());
        }

        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        if (request.role() != null && !request.role().isBlank()) {
            user.setRole(normalizeRole(request.role()));
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User", id);
        }
        userRepository.deleteById(id);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "ROLE_USER";
        }
        String normalized = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        if (!ALLOWED_ROLES.contains(normalized)) {
            throw new IllegalArgumentException("Invalid role: " + role + ". Allowed: ROLE_USER, ROLE_ADMIN");
        }
        return normalized;
    }
}
