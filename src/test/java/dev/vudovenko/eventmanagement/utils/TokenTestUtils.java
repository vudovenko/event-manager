package dev.vudovenko.eventmanagement.utils;

import dev.vudovenko.eventmanagement.security.jwt.JwtTokenManager;
import dev.vudovenko.eventmanagement.users.domain.User;
import dev.vudovenko.eventmanagement.users.services.DefaultUserInitializer;
import dev.vudovenko.eventmanagement.users.userRoles.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TokenTestUtils {

    @Autowired
    private JwtTokenManager jwtTokenManager;
    @Autowired
    private DefaultUserInitializer defaultUserInitializer;

    public String getJwtTokenWithRole(UserRole userRole) {
        defaultUserInitializer.createDefaultUsers();

        return switch (userRole) {
            case ADMIN -> jwtTokenManager.
                    generateToken(DefaultUserInitializer.DEFAULT_ADMIN_LOGIN, UserRole.ADMIN);
            case USER -> jwtTokenManager
                    .generateToken(DefaultUserInitializer.DEFAULT_USER_LOGIN, UserRole.USER);
        };
    }

    public String getJwtToken(User user) {
        return jwtTokenManager.generateToken(
                user.getLogin(),
                user.getRole()
        );
    }
}
