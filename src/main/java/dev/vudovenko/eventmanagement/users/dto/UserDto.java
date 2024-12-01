package dev.vudovenko.eventmanagement.users.dto;

import dev.vudovenko.eventmanagement.users.userRoles.UserRole;

public record UserDto(

        Long id,
        String login,
        Integer age,
        UserRole role
) {
}
