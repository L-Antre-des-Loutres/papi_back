package org.antredesloutres.papi.dto.response;

import org.antredesloutres.papi.model.domain.User;

public record UserResponse(Long id, String username, String role) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole());
    }
}
