package dev.vudovenko.eventmanagement.users.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SignUpRequest(


        @NotBlank
        String login,

        @NotBlank
        String password,

        @NotNull()
        @Min(18)
        Integer age
) {
}
