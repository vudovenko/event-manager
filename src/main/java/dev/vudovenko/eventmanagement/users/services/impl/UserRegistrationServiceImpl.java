package dev.vudovenko.eventmanagement.users.services.impl;

import dev.vudovenko.eventmanagement.users.domain.User;
import dev.vudovenko.eventmanagement.users.dto.UserRegistration;
import dev.vudovenko.eventmanagement.users.exceptions.LoginAlreadyTakenException;
import dev.vudovenko.eventmanagement.users.services.UserRegistrationService;
import dev.vudovenko.eventmanagement.users.services.UserService;
import dev.vudovenko.eventmanagement.users.userRoles.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRegistrationServiceImpl implements UserRegistrationService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User registerUser(UserRegistration userRegistration) {
        if (userService.existsByLogin(userRegistration.login())) {
            throw new LoginAlreadyTakenException(userRegistration.login());
        }

        String hashedPassword = passwordEncoder.encode(userRegistration.password());

        User userToSave = new User(
                null,
                userRegistration.login(),
                hashedPassword,
                userRegistration.age(),
                UserRole.USER
        );


        return userService.save(userToSave);
    }
}
