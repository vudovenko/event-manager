package dev.vudovenko.eventmanagement;

import dev.vudovenko.eventmanagement.security.jwt.JwtTokenManager;
import dev.vudovenko.eventmanagement.users.entity.UserEntity;
import dev.vudovenko.eventmanagement.users.repositories.UserRepository;
import dev.vudovenko.eventmanagement.users.userRoles.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserTestUtils {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenManager jwtTokenManager;

    private static final String DEFAULT_ADMIN_LOGIN = "admin";
    private static final String DEFAULT_USER_LOGIN = "user";
    private static volatile boolean isUsersInitialized = false;

    public String getJwtTokenWithRole(UserRole userRole) {
        if (!isUsersInitialized) {
            initializeTestUsers();
            isUsersInitialized = true;
        }
        return switch (userRole) {
            case ADMIN -> jwtTokenManager.generateToken(DEFAULT_ADMIN_LOGIN);
            case USER -> jwtTokenManager.generateToken(DEFAULT_USER_LOGIN);
        };
    }

    private void initializeTestUsers() {
        createUser(DEFAULT_ADMIN_LOGIN, "admin", 20, UserRole.ADMIN);
        createUser(DEFAULT_USER_LOGIN, "user", 40, UserRole.USER);
    }

    private void createUser(
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
