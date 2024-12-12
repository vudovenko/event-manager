package dev.vudovenko.eventmanagement.utils.dataProviders;

import dev.vudovenko.eventmanagement.users.services.DefaultUserInitializer;
import dev.vudovenko.eventmanagement.users.userRoles.UserRole;

import java.util.stream.Stream;

public class UserTestDataProviders {

    public static Stream<UserRole> rolesProvider() {
        return Stream.of(UserRole.ADMIN, UserRole.USER);
    }

    public static Stream<String> defaultUserLoginsProvider() {
        return Stream.of(
                DefaultUserInitializer.DEFAULT_USER_LOGIN,
                DefaultUserInitializer.DEFAULT_ADMIN_LOGIN
        );
    }
}
