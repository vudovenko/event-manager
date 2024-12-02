package dev.vudovenko.eventmanagement.users.dto;

import jakarta.validation.constraints.NotBlank;

public record UserCredentials(

        @NotBlank
        String login,
        @NotBlank
        String password
) {
}
