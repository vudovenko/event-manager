package dev.vudovenko.eventmanagement.users.services;

import dev.vudovenko.eventmanagement.users.entity.UserEntity;
import dev.vudovenko.eventmanagement.users.repositories.UserRepository;
import dev.vudovenko.eventmanagement.users.userRoles.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultUserInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public static final String DEFAULT_ADMIN_LOGIN = "admin";
    public static final String DEFAULT_USER_LOGIN = "user";

    private static volatile boolean isUsersInitialized = false;

    @EventListener(ContextRefreshedEvent.class)
    public void createDefaultUsers() {
        synchronized (DefaultUserInitializer.class) {
            if (!isUsersInitialized) {
                createUserIfNotExists(DEFAULT_ADMIN_LOGIN, "admin", 20, UserRole.ADMIN);
                createUserIfNotExists(DEFAULT_USER_LOGIN, "user", 40, UserRole.USER);

                isUsersInitialized = true;
            }
        }
    }

    private void createUserIfNotExists(
            String login,
            String password,
            Integer age,
            UserRole role
    ) {
        if (userRepository.existsByLogin(login)) {
            return;
        }

        String hashedPass = passwordEncoder.encode(password);
        UserEntity userToSave = new UserEntity(
                null,
                login,
                hashedPass,
                age,
                role
        );

        userRepository.save(userToSave);
    }
}