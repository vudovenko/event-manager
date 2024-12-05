package dev.vudovenko.eventmanagement.users.services;

import dev.vudovenko.eventmanagement.users.domain.User;

public interface UserService {

    User save(User user);

    boolean existsByLogin(String login);

    boolean existsById(Long id);

    User findByLogin(String login);

    User findById(Long id);
}
