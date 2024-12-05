package dev.vudovenko.eventmanagement.users.mappers;

import dev.vudovenko.eventmanagement.common.mappers.ToDomainMapper;
import dev.vudovenko.eventmanagement.users.domain.User;
import dev.vudovenko.eventmanagement.users.dto.UserRegistration;
import dev.vudovenko.eventmanagement.users.userRoles.UserRole;
import org.springframework.stereotype.Component;

@Component
public class UserRegistrationMapper implements ToDomainMapper<User, UserRegistration> {

    @Override
    public User toDomain(UserRegistration userRegistration) {
        return new User(
                null,
                userRegistration.login(),
                userRegistration.password(),
                userRegistration.age(),
                UserRole.USER
        );
    }
}
