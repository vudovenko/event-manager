package dev.vudovenko.eventmanagement.users.mappers;

import dev.vudovenko.eventmanagement.common.mappers.DtoMapper;
import dev.vudovenko.eventmanagement.users.domain.User;
import dev.vudovenko.eventmanagement.users.dto.UserDto;
import org.springframework.stereotype.Component;

@Component
public class UserDtoMapper implements DtoMapper<User, UserDto> {

    @Override
    public User toDomain(UserDto userDto) {
        return new User(
                userDto.id(),
                userDto.login(),
                null,
                userDto.age(),
                userDto.role()
        );
    }

    @Override
    public UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getLogin(),
                user.getAge(),
                user.getRole()
        );
    }
}
