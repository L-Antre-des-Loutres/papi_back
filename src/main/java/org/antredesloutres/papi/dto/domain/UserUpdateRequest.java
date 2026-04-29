package org.antredesloutres.papi.dto.domain;

import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @Size(min = 3, max = 50) String username,
        @Size(min = 8, max = 100) String password,
        String role
) {}
