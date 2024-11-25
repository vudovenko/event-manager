package dev.vudovenko.eventmanagement.common.exceptionHandling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ErrorMessageResponse(

        @NotBlank
        String message,

        @NotBlank
        String detailedMessage,

        @NotNull
        LocalDateTime dateTime
) {
    public ErrorMessageResponse {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be null or blank");
        }
        if (detailedMessage == null || detailedMessage.isBlank()) {
            throw new IllegalArgumentException("DetailedMessage cannot be null or blank");
        }
        if (dateTime == null) {
            throw new IllegalArgumentException("DateTime cannot be null");
        }
    }

    public static ErrorMessageResponse of(String message, String detailedMessage) {
        return new ErrorMessageResponse(message, detailedMessage, LocalDateTime.now());
    }
}
