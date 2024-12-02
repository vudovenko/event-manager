package dev.vudovenko.eventmanagement.users.services;

import dev.vudovenko.eventmanagement.users.domain.User;
import dev.vudovenko.eventmanagement.users.dto.UserRegistration;

public interface UserService {

    User registerUser(UserRegistration userRegistration);

    User findByLogin(String login);
}
