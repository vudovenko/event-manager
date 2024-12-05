package dev.vudovenko.eventmanagement.users.controllers;

import dev.vudovenko.eventmanagement.common.mappers.DtoMapper;
import dev.vudovenko.eventmanagement.common.mappers.ToDomainMapper;
import dev.vudovenko.eventmanagement.security.jwt.JwtAuthenticationService;
import dev.vudovenko.eventmanagement.security.jwt.dto.JwtTokenResponse;
import dev.vudovenko.eventmanagement.users.domain.User;
import dev.vudovenko.eventmanagement.users.dto.UserCredentials;
import dev.vudovenko.eventmanagement.users.dto.UserDto;
import dev.vudovenko.eventmanagement.users.dto.UserRegistration;
import dev.vudovenko.eventmanagement.users.services.UserRegistrationService;
import dev.vudovenko.eventmanagement.users.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserRegistrationService userRegistrationService;
    private final JwtAuthenticationService jwtAuthenticationService;
    private final ToDomainMapper<User, UserRegistration> userRegistrationMapper;
    private final DtoMapper<User, UserDto> userDtoMapper;

    @PostMapping
    public ResponseEntity<UserDto> createUser(
            @Valid @RequestBody UserRegistration userRegistration
    ) {
        log.info("Get request for sign-up: login={}", userRegistration.login());
        User user = userRegistrationService.registerUser(
                userRegistrationMapper.toDomain(userRegistration)
        );

        return ResponseEntity
                .status(201)
                .body(userDtoMapper.toDto(user));
    }

    @PostMapping("/auth")
    public ResponseEntity<JwtTokenResponse> authenticate(
            @Valid @RequestBody UserCredentials userCredentials
    ) {
        log.info("Get request for sign-in: login={}", userCredentials.login());
        String token = jwtAuthenticationService.authenticateUser(userCredentials);

        return ResponseEntity.ok(new JwtTokenResponse(token));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(
            @PathVariable("userId") Long userId
    ) {
        log.info("Get request for get user by id: id={}", userId);
        User user = userService.findById(userId);

        return ResponseEntity.ok(userDtoMapper.toDto(user));
    }
}
