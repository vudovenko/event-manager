package dev.vudovenko.eventmanagement.locations.exceptions.exceptionHandlers;

import dev.vudovenko.eventmanagement.common.exceptionHandling.dto.ErrorMessageResponse;
import dev.vudovenko.eventmanagement.common.exceptionHandling.exceptionMessages.ExceptionHandlerMessages;
import dev.vudovenko.eventmanagement.locations.exceptions.LocationCapacityIsLowerThanItWasException;
import dev.vudovenko.eventmanagement.locations.exceptions.LocationNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Log4j2
@ControllerAdvice
@Order(Integer.MIN_VALUE)
public class LocationExceptionHandler {

    @ExceptionHandler(value = LocationNotFoundException.class)
    public ResponseEntity<ErrorMessageResponse> handleEntityNotFoundException(
            RuntimeException e
    ) {
        log.error("Got entity not found exception", e);
        ErrorMessageResponse errorDto = ErrorMessageResponse.of(
                ExceptionHandlerMessages.ENTITY_NOT_FOUND.getMessage(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorDto);
    }

    @ExceptionHandler(value = LocationCapacityIsLowerThanItWasException.class)
    public ResponseEntity<ErrorMessageResponse>
    handleLocationCapacityIsLowerThanItWasException(
            LocationCapacityIsLowerThanItWasException e
    ) {
        log.error("Got location capacity is lower than it was exception", e);
        ErrorMessageResponse errorDto = ErrorMessageResponse.of(
                ExceptionHandlerMessages.LOCATION_CAPACITY_IS_LOWER_THAN_IT_WAS.getMessage(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }
}
