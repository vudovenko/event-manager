package dev.vudovenko.eventmanagement;

import dev.vudovenko.eventmanagement.security.jwt.JwtTokenManager;
import dev.vudovenko.eventmanagement.users.services.DefaultUserInitializer;
import dev.vudovenko.eventmanagement.users.userRoles.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserTestUtils {

    private final JwtTokenManager jwtTokenManager;
    private final DefaultUserInitializer defaultUserInitializer;

    public String getJwtTokenWithRole(UserRole userRole) {
        defaultUserInitializer.createDefaultUsers();

        return switch (userRole) {
            case ADMIN -> jwtTokenManager.
                    generateToken(DefaultUserInitializer.DEFAULT_ADMIN_LOGIN);
            case USER -> jwtTokenManager
                    .generateToken(DefaultUserInitializer.DEFAULT_USER_LOGIN);
        };
    }
}
