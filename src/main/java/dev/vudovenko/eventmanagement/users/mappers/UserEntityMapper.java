package dev.vudovenko.eventmanagement.users.mappers;

import dev.vudovenko.eventmanagement.common.mappers.EntityMapper;
import dev.vudovenko.eventmanagement.users.domain.User;
import dev.vudovenko.eventmanagement.users.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserEntityMapper implements EntityMapper<User, UserEntity> {

    @Override
    public User toDomain(UserEntity userEntity) {
        return new User(
                userEntity.getId(),
                userEntity.getLogin(),
                userEntity.getPasswordHash(),
                userEntity.getAge(),
                userEntity.getRole()
        );
    }

    @Override
    public UserEntity toEntity(User user) {
        return new UserEntity(
                user.getId(),
                user.getLogin(),
                user.getPasswordHash(),
                user.getAge(),
                user.getRole()
        );
    }
}
