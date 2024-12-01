package dev.vudovenko.eventmanagement.users.controllers;

import dev.vudovenko.eventmanagement.common.mappers.DtoMapper;
import dev.vudovenko.eventmanagement.users.domain.User;
import dev.vudovenko.eventmanagement.users.dto.SignUpRequest;
import dev.vudovenko.eventmanagement.users.dto.UserDto;
import dev.vudovenko.eventmanagement.users.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final DtoMapper<User, UserDto> userDtoMapper;

    @PostMapping
    public ResponseEntity<UserDto> createUser(
            @Valid @RequestBody SignUpRequest signUpRequest
    ) {
        log.info("Get request for sign-up: login={}", signUpRequest.login());
        User user = userService.registerUser(signUpRequest);

        return ResponseEntity
                .status(201)
                .body(userDtoMapper.toDto(user));
    }
}
