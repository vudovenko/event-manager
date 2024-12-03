package dev.vudovenko.eventmanagement.events.exceptions.exceptionHandlers;

import dev.vudovenko.eventmanagement.common.exceptionHandling.dto.ErrorMessageResponse;
import dev.vudovenko.eventmanagement.common.exceptionHandling.exceptionMessages.ExceptionHandlerMessages;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.format.DateTimeParseException;

@Log4j2
@ControllerAdvice
@Order(Integer.MIN_VALUE)
public class EventExceptionHandler {

    @ExceptionHandler(value = DateTimeParseException.class)
    public ResponseEntity<ErrorMessageResponse> handleDateTimeParseException(
            RuntimeException e
    ) {
        log.error("Got date time parse exception", e);
        ErrorMessageResponse errorDto = ErrorMessageResponse.of(
                ExceptionHandlerMessages.DATE_TIME_PARSE_EXCEPTION.getMessage(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }
}
