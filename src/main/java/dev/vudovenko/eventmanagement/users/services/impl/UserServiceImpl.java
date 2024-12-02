package dev.vudovenko.eventmanagement.users.services.impl;

import dev.vudovenko.eventmanagement.common.mappers.EntityMapper;
import dev.vudovenko.eventmanagement.users.domain.User;
import dev.vudovenko.eventmanagement.users.dto.SignUpRequest;
import dev.vudovenko.eventmanagement.users.entity.UserEntity;
import dev.vudovenko.eventmanagement.users.exceptions.LoginAlreadyTakenException;
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
    public User registerUser(SignUpRequest signUpRequest) {
        if (userRepository.existsByLogin(signUpRequest.login())) {
            throw new LoginAlreadyTakenException(signUpRequest.login());
        }

        String hashedPassword = passwordEncoder.encode(signUpRequest.password());

        UserEntity userToSave = userRepository.save(
                new UserEntity(
                        null,
                        signUpRequest.login(),
                        hashedPassword,
                        signUpRequest.age(),
                        UserRole.USER
                )
        );

        return userEntityMapper.toDomain(userToSave);
    }

    @Override
    public User findByLogin(String login) {
        UserEntity user = userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return userEntityMapper.toDomain(user);
    }
}
