package dev.vudovenko.eventmanagement.utils;

import dev.vudovenko.eventmanagement.users.domain.User;
import dev.vudovenko.eventmanagement.users.dto.UserRegistration;
import dev.vudovenko.eventmanagement.users.services.UserRegistrationService;
import dev.vudovenko.eventmanagement.users.userRoles.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserTestUtils {

    @Autowired
    private UserRegistrationService userRegistrationService;

    public User getRegisteredUser() {
        return userRegistrationService.registerUser(
                new User(
                        null,
                        "login-" + RandomUtils.getRandomInt(),
                        "password-" + RandomUtils.getRandomInt(),
                        20,
                        UserRole.USER
                )
        );
    }

    public UserRegistration getWrongUserRegistration() {
        return new UserRegistration(
                null,
                "    ",
                10
        );
    }
}
