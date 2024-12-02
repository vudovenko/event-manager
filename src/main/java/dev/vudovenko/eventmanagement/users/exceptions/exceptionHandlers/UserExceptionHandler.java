package dev.vudovenko.eventmanagement.users.exceptions.exceptionHandlers;

import dev.vudovenko.eventmanagement.common.exceptionHandling.dto.ErrorMessageResponse;
import dev.vudovenko.eventmanagement.common.exceptionHandling.exceptionMessages.ExceptionHandlerMessages;
import dev.vudovenko.eventmanagement.users.exceptions.LoginAlreadyTakenException;
import dev.vudovenko.eventmanagement.users.exceptions.UserIdNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Log4j2
@ControllerAdvice
@Order(Integer.MIN_VALUE)
public class UserExceptionHandler {

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
}
