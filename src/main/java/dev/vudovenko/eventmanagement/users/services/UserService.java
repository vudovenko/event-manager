package dev.vudovenko.eventmanagement.users.services;

import dev.vudovenko.eventmanagement.users.domain.User;

public interface UserService {

    User save(User user);

    Boolean existsByLogin(String login);

    User findByLogin(String login);

    User findById(Long id);
}
