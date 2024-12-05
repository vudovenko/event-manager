package dev.vudovenko.eventmanagement.users.services.impl;

import dev.vudovenko.eventmanagement.common.mappers.EntityMapper;
import dev.vudovenko.eventmanagement.users.domain.User;
import dev.vudovenko.eventmanagement.users.entity.UserEntity;
import dev.vudovenko.eventmanagement.users.exceptions.UserIdNotFoundException;
import dev.vudovenko.eventmanagement.users.repositories.UserRepository;
import dev.vudovenko.eventmanagement.users.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EntityMapper<User, UserEntity> userEntityMapper;

    @Override
    public User save(User user) {
        UserEntity userEntity = userRepository.save(
                userEntityMapper.toEntity(user)
        );

        return userEntityMapper.toDomain(userEntity);
    }

    @Override
    public boolean existsByLogin(String login) {
        return userRepository.existsByLogin(login);
    }

    @Override
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
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
