package dev.vudovenko.eventmanagement.users.services.impl;

import dev.vudovenko.eventmanagement.common.mappers.EntityMapper;
import dev.vudovenko.eventmanagement.users.domain.User;
import dev.vudovenko.eventmanagement.users.dto.UserRegistration;
import dev.vudovenko.eventmanagement.users.entity.UserEntity;
import dev.vudovenko.eventmanagement.users.exceptions.LoginAlreadyTakenException;
import dev.vudovenko.eventmanagement.users.exceptions.UserIdNotFoundException;
import dev.vudovenko.eventmanagement.users.repositories.UserRepository;
import dev.vudovenko.eventmanagement.users.services.UserService;
import dev.vudovenko.eventmanagement.users.userRoles.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EntityMapper<User, UserEntity> userEntityMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User registerUser(UserRegistration userRegistration) {
        if (userRepository.existsByLogin(userRegistration.login())) {
            throw new LoginAlreadyTakenException(userRegistration.login());
        }

        String hashedPassword = passwordEncoder.encode(userRegistration.password());

        UserEntity userToSave = userRepository.save(
                new UserEntity(
                        null,
                        userRegistration.login(),
                        hashedPassword,
                        userRegistration.age(),
                        UserRole.USER
                )
        );

        return userEntityMapper.toDomain(userToSave);
    }

    @Override
    public User findByLogin(String login) {
        UserEntity userEntity = userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("User with login %s not found".formatted(login)));

        return userEntityMapper.toDomain(userEntity);
    }

    @Override
    public User findById(Long userId) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserIdNotFoundException(userId));

        return userEntityMapper.toDomain(userEntity);
    }
}
