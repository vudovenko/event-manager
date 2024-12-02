package dev.vudovenko.eventmanagement.common.exceptionHandling.handler;

import dev.vudovenko.eventmanagement.common.exceptionHandling.dto.ErrorMessageResponse;
import dev.vudovenko.eventmanagement.common.exceptionHandling.exceptionMessages.ExceptionHandlerMessages;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

@Log4j2
@ControllerAdvice
@Order
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
