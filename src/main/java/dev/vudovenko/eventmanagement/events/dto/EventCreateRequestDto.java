package dev.vudovenko.eventmanagement.events.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record EventCreateRequestDto(

        @NotBlank
        String name,

        @NotNull
        @Min(0)
        Integer maxPlaces,

        @NotNull
        LocalDateTime date,

        @NotNull
        @Min(0)
        Integer cost,

        @NotNull
        @Min(30)
        Integer duration,

        @NotNull
        Long locationId
) {
}
