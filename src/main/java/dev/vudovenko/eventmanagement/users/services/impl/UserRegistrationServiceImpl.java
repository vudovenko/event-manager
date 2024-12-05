package dev.vudovenko.eventmanagement.users.services.impl;

import dev.vudovenko.eventmanagement.users.domain.User;
import dev.vudovenko.eventmanagement.users.exceptions.LoginAlreadyTakenException;
import dev.vudovenko.eventmanagement.users.services.UserRegistrationService;
import dev.vudovenko.eventmanagement.users.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRegistrationServiceImpl implements UserRegistrationService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User registerUser(User user) {
        if (userService.existsByLogin(user.getLogin())) {
            throw new LoginAlreadyTakenException(user.getLogin());
        }

        String hashedPassword = passwordEncoder.encode(user.getPasswordHash());
        user.setPasswordHash(hashedPassword);

        return userService.save(user);
    }
}
