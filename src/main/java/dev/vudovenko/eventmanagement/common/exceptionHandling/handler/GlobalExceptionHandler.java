package dev.vudovenko.eventmanagement.common.exceptionHandling.handler;

import dev.vudovenko.eventmanagement.common.exceptionHandling.dto.ErrorMessageResponse;
import dev.vudovenko.eventmanagement.common.exceptionHandling.exceptionMessages.ExceptionHandlerMessages;
import dev.vudovenko.eventmanagement.locations.exceptions.LocationCapacityIsLowerThanItWasException;
import dev.vudovenko.eventmanagement.locations.exceptions.LocationNotFoundException;
import dev.vudovenko.eventmanagement.users.exceptions.LoginAlreadyTakenException;
import dev.vudovenko.eventmanagement.users.exceptions.UserIdNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

@Log4j2
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorMessageResponse> handleGenericException(
            Exception e
    ) {
        log.error("Got server error", e);
        ErrorMessageResponse errorDto = ErrorMessageResponse.of(
                ExceptionHandlerMessages.SERVER_ERROR.getMessage(),
                e.getMessage() != null
                        ? e.getMessage()
                        : "Unknown error"
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorDto);
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorMessageResponse> handleValidationException(
            MethodArgumentNotValidException e
    ) {
        log.error("Got validation exception", e);

        String detailedMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorMessageResponse errorDto = ErrorMessageResponse.of(
                ExceptionHandlerMessages.VALIDATION_FAILED.getMessage(),
                detailedMessage
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

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

    @ExceptionHandler(value = UsernameNotFoundException.class)
    public ResponseEntity<ErrorMessageResponse> handleUsernameNotFoundException(
            UsernameNotFoundException e
    ) {
        log.error("Got username not found exception", e);
        ErrorMessageResponse errorDto = ErrorMessageResponse.of(
                ExceptionHandlerMessages.USERNAME_NOT_FOUND.getMessage(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorDto);
    }

    @ExceptionHandler(value = UserIdNotFoundException.class)
    public ResponseEntity<ErrorMessageResponse> handleUserIdNotFoundException(
            UserIdNotFoundException e
    ) {
        log.error("Got user id not found exception", e);
        ErrorMessageResponse errorDto = ErrorMessageResponse.of(
                ExceptionHandlerMessages.USER_ID_NOT_FOUND.getMessage(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorDto);
    }

    @ExceptionHandler(value = LoginAlreadyTakenException.class)
    public ResponseEntity<ErrorMessageResponse> handleLoginAlreadyTakenException(
            LoginAlreadyTakenException e
    ) {
        log.error("Got login already taken exception", e);
        ErrorMessageResponse errorDto = ErrorMessageResponse.of(
                ExceptionHandlerMessages.LOGIN_ALREADY_TAKEN.getMessage(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorMessageResponse> handleAuthenticationException(
            BadCredentialsException e
    ) {
        log.error("Handle authentication exception", e);
        ErrorMessageResponse errorDto = ErrorMessageResponse.of(
                ExceptionHandlerMessages.FAILED_TO_AUTHENTICATE.getMessage(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorDto);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorMessageResponse> handleAuthorizationException(
            AuthorizationDeniedException e
    ) {
        log.error("Handle authorization exception", e);
        ErrorMessageResponse errorDto = ErrorMessageResponse.of(
                ExceptionHandlerMessages.FORBIDDEN.getMessage(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(errorDto);
    }
}
