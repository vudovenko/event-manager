package dev.vudovenko.eventmanagement.events.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EventCreateRequestDto(

        @NotBlank
        String name,

        @NotNull
        @Min(0)
        Integer maxPlaces,

        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        String date,

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
